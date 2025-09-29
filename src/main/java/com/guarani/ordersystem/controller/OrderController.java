package com.guarani.ordersystem.controller;

import com.guarani.ordersystem.dto.*;
import com.guarani.ordersystem.entity.enums.OrderStatus;
import com.guarani.ordersystem.service.OrderService;
import com.guarani.ordersystem.util.Constants;
import com.guarani.ordersystem.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "APIs para gerenciamento de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Retorna lista paginada de pedidos")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> orders;
        if (SecurityUtils.hasRole("ADMIN")) {
            orders = orderService.findAll(pageable);
        } else {
            String currentUser = SecurityUtils.getCurrentUsername();
            orders = orderService.findByUserEmail(currentUser, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(orders)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter pedido por ID", description = "Retorna pedido específico por ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Pedido encontrado", order));
    }

    @GetMapping("/user/{userEmail}")
    @PreAuthorize("hasRole('ADMIN') or #userEmail == authentication.principal.username")
    @Operation(summary = "Listar pedidos por usuário", description = "Retorna pedidos de usuário específico")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrdersByUser(
            @PathVariable String userEmail,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderResponse> orders = orderService.findByUserEmail(userEmail, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(orders)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Listar pedidos por status", description = "Retorna pedidos com status específico")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderResponse> orders = orderService.findByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(orders)));
    }

    @PostMapping
    @Operation(summary = "Criar pedido", description = "Cria um novo pedido para o usuário autenticado")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest request
    ) {
        String userEmail = SecurityUtils.getCurrentUsername();
        OrderResponse order = orderService.create(request, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Pedido criado com sucesso", order));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar status do pedido", description = "Atualiza status de um pedido existente")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderResponse order = orderService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Status do pedido atualizado", order));
    }

    @PatchMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar status de pagamento", description = "Atualiza status de pagamento do pedido")
    public ResponseEntity<ApiResponse<OrderResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderPaymentUpdateRequest request
    ) {
        OrderResponse order = orderService.updatePaymentStatus(
                id, request.getPaymentStatus(), request.getPaymentMethod(), request.getTransactionId());
        return ResponseEntity.ok(ApiResponse.success("Status de pagamento atualizado", order));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar pedido", description = "Cancela um pedido existente")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Pedido cancelado com sucesso"));
    }
}