package com.guarani.ordersystem.service;

import com.guarani.ordersystem.dto.OrderRequest;
import com.guarani.ordersystem.dto.OrderResponse;
import com.guarani.ordersystem.entity.*;
import com.guarani.ordersystem.entity.enums.OrderStatus;
import com.guarani.ordersystem.entity.enums.PaymentMethod;
import com.guarani.ordersystem.entity.enums.PaymentStatus;
import com.guarani.ordersystem.entity.enums.StockMovementType;
import com.guarani.ordersystem.exception.BusinessException;
import com.guarani.ordersystem.exception.OrderProcessingException;
import com.guarani.ordersystem.exception.ResourceNotFoundException;
import com.guarani.ordersystem.repository.OrderRepository;
import com.guarani.ordersystem.repository.ProductRepository;
import com.guarani.ordersystem.repository.UserRepository;
import com.guarani.ordersystem.util.Constants;
import com.guarani.ordersystem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "orders")
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Cacheable(key = "#id")
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        log.info("Buscando pedido por ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));

        // Verificar se o usuário tem permissão para ver o pedido
        checkOrderAccessPermission(order);

        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findByUserEmail(String userEmail, Pageable pageable) {
        log.info("Buscando pedidos do usuário: {}", userEmail);

        // Verificar se o usuário está acessando seus próprios pedidos ou é admin
        String currentUser = SecurityUtils.getCurrentUsername();
        if (!currentUser.equals(userEmail) && !SecurityUtils.hasRole("ADMIN")) {
            throw new BusinessException("Acesso negado para visualizar pedidos de outros usuários");
        }

        return orderRepository.findByUserEmail(userEmail, pageable)
                .map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findByStatus(OrderStatus status, Pageable pageable) {
        log.info("Buscando pedidos por status: {}", status);
        return orderRepository.findByStatus(status, pageable)
                .map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Pageable pageable) {
        log.info("Buscando todos os pedidos paginados");
        return orderRepository.findAll(pageable)
                .map(this::mapToOrderResponse);
    }

    @CacheEvict(allEntries = true)
    @Transactional
    public OrderResponse create(OrderRequest request, String userEmail) {
        log.info("Criando novo pedido para usuário: {}", userEmail);

        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", "email", userEmail));

            Order order = Order.builder()
                    .user(user)
                    .status(OrderStatus.PENDING)
                    .paymentStatus(PaymentStatus.PENDING)
                    .items(new ArrayList<>())
                    .build();

            // Processar itens do pedido
            for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", itemRequest.getProductId()));

                if (!product.getActive()) {
                    throw new BusinessException("Produto não está disponível: " + product.getName());
                }

                if (product.getStockQuantity() < itemRequest.getQuantity()) {
                    throw new BusinessException("Estoque insuficiente para o produto: " + product.getName());
                }

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(product.getPrice())
                        .build();
                orderItem.calculateTotalPrice();

                order.addItem(orderItem);

                // Atualizar estoque
                productService.updateStock(
                        product.getId(),
                        itemRequest.getQuantity(),
                        StockMovementType.SAIDA,
                        "Venda - Pedido " + order.getId()
                );
            }

            order.recalculateTotalAmount();
            Order savedOrder = orderRepository.save(order);

            log.info("Pedido criado com ID: {}", savedOrder.getId());
            return mapToOrderResponse(savedOrder);

        } catch (Exception e) {
            log.error("Erro ao criar pedido para usuário {}: {}", userEmail, e.getMessage(), e);
            throw new OrderProcessingException("Erro ao processar pedido: " + e.getMessage(),
                    Constants.ERROR_ORDER_PROCESSING, null);
        }
    }

    @CacheEvict(key = "#id")
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        log.info("Atualizando status do pedido ID: {} para {}", id, newStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        // Lógica específica para mudanças de status
        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            // Restaurar estoque se o pedido for cancelado
            restoreStockForOrder(order);
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Status do pedido ID: {} atualizado de {} para {}", id, oldStatus, newStatus);

        return mapToOrderResponse(updatedOrder);
    }

    @CacheEvict(key = "#id")
    @Transactional
    public OrderResponse updatePaymentStatus(Long id, PaymentStatus paymentStatus, PaymentMethod paymentMethod, String transactionId) {
        log.info("Atualizando status de pagamento do pedido ID: {} para {}", id, paymentStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));

        order.setPaymentStatus(paymentStatus);
        order.setPaymentMethod(paymentMethod);
        order.setTransactionId(transactionId);

        if (paymentStatus == PaymentStatus.APPROVED) {
            order.setPaymentDate(LocalDateTime.now());
            // Se o pagamento for aprovado, confirmar o pedido automaticamente
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
            }
        }

        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    @CacheEvict(key = "#id")
    @Transactional
    public void cancelOrder(Long id) {
        log.info("Cancelando pedido ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));

        if (!order.canBeCancelled()) {
            throw new BusinessException("Pedido não pode ser cancelado no status atual: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        restoreStockForOrder(order);

        orderRepository.save(order);
        log.info("Pedido ID: {} cancelado com sucesso", id);
    }

    private void restoreStockForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            productService.updateStock(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    StockMovementType.ENTRADA,
                    "Cancelamento do pedido " + order.getId()
            );
        }
    }

    private void checkOrderAccessPermission(Order order) {
        String currentUser = SecurityUtils.getCurrentUsername();
        if (!order.getUser().getEmail().equals(currentUser) && !SecurityUtils.hasRole("ADMIN")) {
            throw new BusinessException("Acesso negado para visualizar este pedido");
        }
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userEmail(order.getUser().getEmail())
                .userName(order.getUser().getName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .paymentDate(order.getPaymentDate())
                .transactionId(order.getTransactionId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems().stream()
                        .map(this::mapToOrderItemResponse)
                        .toList())
                .build();
    }

    private OrderResponse.OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderResponse.OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}