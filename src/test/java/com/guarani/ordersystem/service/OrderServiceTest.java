package com.guarani.ordersystem.service;

import com.guarani.ordersystem.dto.OrderRequest;
import com.guarani.ordersystem.dto.OrderResponse;
import com.guarani.ordersystem.entity.*;
import com.guarani.ordersystem.entity.enums.OrderStatus;
import com.guarani.ordersystem.entity.enums.PaymentStatus;
import com.guarani.ordersystem.exception.BusinessException;
import com.guarani.ordersystem.exception.ResourceNotFoundException;
import com.guarani.ordersystem.repository.OrderRepository;
import com.guarani.ordersystem.repository.ProductRepository;
import com.guarani.ordersystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void create_ShouldCreateOrder_WhenValidRequest() {
        // Arrange
        String userEmail = "test@email.com";
        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .name("Test User")
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(100.0))
                .stockQuantity(10)
                .active(true)
                .build();

        OrderRequest.OrderItemRequest itemRequest = OrderRequest.OrderItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();

        OrderRequest request = OrderRequest.builder()
                .items(List.of(itemRequest))
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse result = orderService.create(request, userEmail);

        // Assert
        assertNotNull(result);
        assertEquals(userEmail, result.getUserEmail());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productService, times(1)).updateStock(eq(1L), eq(2), any(), anyString());
    }

    @Test
    void create_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        String userEmail = "test@email.com";
        User user = User.builder().id(1L).email(userEmail).build();

        OrderRequest.OrderItemRequest itemRequest = OrderRequest.OrderItemRequest.builder()
                .productId(999L)
                .quantity(1)
                .build();

        OrderRequest request = OrderRequest.builder()
                .items(List.of(itemRequest))
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.create(request, userEmail));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void create_ShouldThrowException_WhenInsufficientStock() {
        // Arrange
        String userEmail = "test@email.com";
        User user = User.builder().id(1L).email(userEmail).build();

        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(100.0))
                .stockQuantity(1) // Only 1 in stock
                .active(true)
                .build();

        OrderRequest.OrderItemRequest itemRequest = OrderRequest.OrderItemRequest.builder()
                .productId(1L)
                .quantity(2) // Requesting 2
                .build();

        OrderRequest request = OrderRequest.builder()
                .items(List.of(itemRequest))
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(BusinessException.class, () -> orderService.create(request, userEmail));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void findById_ShouldReturnOrder_WhenOrderExists() {
        // Arrange
        Long orderId = 1L;
        User user = User.builder()
                .id(1L)
                .email("test@email.com")
                .build();

        Order order = Order.builder()
                .id(orderId)
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(200.0))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        OrderResponse result = orderService.findById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("test@email.com", result.getUserEmail());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void updateStatus_ShouldUpdateOrderStatus() {
        // Arrange
        Long orderId = 1L;
        Order order = Order.builder()
                .id(orderId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse result = orderService.updateStatus(orderId, OrderStatus.CONFIRMED);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void cancelOrder_ShouldCancelOrder_WhenOrderCanBeCancelled() {
        // Arrange
        Long orderId = 1L;
        Order order = Order.builder()
                .id(orderId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.cancelOrder(orderId);

        // Assert
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }
}