package com.guarani.ordersystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarani.ordersystem.dto.*;
import com.guarani.ordersystem.entity.enums.OrderStatus;
import com.guarani.ordersystem.entity.enums.PaymentMethod;
import com.guarani.ordersystem.entity.enums.PaymentStatus;
import com.guarani.ordersystem.exception.ResourceNotFoundException;
import com.guarani.ordersystem.service.OrderService;
import com.guarani.ordersystem.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private SecurityUtils securityUtils;

    @Test
    @WithMockUser
    void getAllOrders_ShouldReturnUserOrders_WhenClient() throws Exception {
        // Arrange
        OrderResponse order = createOrderResponse(1L, "user@email.com", OrderStatus.PENDING);

        Page<OrderResponse> page = new PageImpl<>(List.of(order));
        when(orderService.findByUserEmail(eq("user@email.com"), any())).thenReturn(page);
        when(securityUtils.getCurrentUsername()).thenReturn("user@email.com");
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].userEmail").value("user@email.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_ShouldReturnAllOrders_WhenAdmin() throws Exception {
        // Arrange
        OrderResponse order = createOrderResponse(1L, "user@email.com", OrderStatus.PENDING);

        Page<OrderResponse> page = new PageImpl<>(List.of(order));
        when(orderService.findAll(any())).thenReturn(page);
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    @WithMockUser
    void getOrderById_ShouldReturnOrder_WhenOrderExists() throws Exception {
        // Arrange
        OrderResponse order = createOrderResponse(1L, "user@email.com", OrderStatus.PENDING);
        when(orderService.findById(1L)).thenReturn(order);
        when(securityUtils.getCurrentUsername()).thenReturn("user@email.com");

        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido encontrado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void getOrderById_ShouldReturnNotFound_WhenOrderNotExists() throws Exception {
        // Arrange
        when(orderService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Pedido", "id", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void getOrdersByUser_ShouldReturnOrders_WhenOwner() throws Exception {
        // Arrange
        OrderResponse order = createOrderResponse(1L, "user@email.com", OrderStatus.PENDING);

        Page<OrderResponse> page = new PageImpl<>(List.of(order));
        when(orderService.findByUserEmail(eq("user@email.com"), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/orders/user/user@email.com")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userEmail").value("user@email.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getOrdersByUser_ShouldReturnOrders_WhenAdmin() throws Exception {
        // Arrange
        OrderResponse order = createOrderResponse(1L, "other@email.com", OrderStatus.PENDING);

        Page<OrderResponse> page = new PageImpl<>(List.of(order));
        when(orderService.findByUserEmail(eq("other@email.com"), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/orders/user/other@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void getOrdersByStatus_ShouldReturnOrders_WhenOperator() throws Exception {
        // Arrange
        OrderResponse order = createOrderResponse(1L, "user@email.com", OrderStatus.CONFIRMED);

        Page<OrderResponse> page = new PageImpl<>(List.of(order));
        when(orderService.findByStatus(eq(OrderStatus.CONFIRMED), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/orders/status/CONFIRMED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser
    void createOrder_ShouldCreateOrder_WhenValidRequest() throws Exception {
        // Arrange
        OrderRequest.OrderItemRequest itemRequest = OrderRequest.OrderItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();

        OrderRequest request = OrderRequest.builder()
                .items(List.of(itemRequest))
                .build();

        OrderResponse response = createOrderResponse(1L, "user@email.com", OrderStatus.PENDING);

        when(orderService.create(any(OrderRequest.class), eq("user@email.com"))).thenReturn(response);
        when(securityUtils.getCurrentUsername()).thenReturn("user@email.com");

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido criado com sucesso"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void createOrder_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Arrange
        OrderRequest request = OrderRequest.builder()
                .items(List.of()) // Lista vazia
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void updateOrderStatus_ShouldUpdateStatus_WhenOperator() throws Exception {
        // Arrange
        OrderStatusUpdateRequest request = OrderStatusUpdateRequest.builder()
                .status(OrderStatus.CONFIRMED)
                .build();

        OrderResponse response = createOrderResponse(1L, "user@email.com", OrderStatus.CONFIRMED);
        when(orderService.updateStatus(1L, OrderStatus.CONFIRMED)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/orders/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Status do pedido atualizado"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void updatePaymentStatus_ShouldUpdatePayment_WhenOperator() throws Exception {
        // Arrange
        OrderPaymentUpdateRequest request = OrderPaymentUpdateRequest.builder()
                .paymentStatus(PaymentStatus.APPROVED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .transactionId("txn_123456")
                .build();

        OrderResponse response = createOrderResponse(1L, "user@email.com", OrderStatus.CONFIRMED);
        response.setPaymentStatus(PaymentStatus.APPROVED);
        response.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(orderService.updatePaymentStatus(eq(1L), eq(PaymentStatus.APPROVED),
                eq(PaymentMethod.CREDIT_CARD), eq("txn_123456"))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/orders/1/payment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Status de pagamento atualizado"))
                .andExpect(jsonPath("$.data.paymentStatus").value("APPROVED"))
                .andExpect(jsonPath("$.data.paymentMethod").value("CREDIT_CARD"));
    }

    @Test
    @WithMockUser
    void cancelOrder_ShouldCancelOrder_WhenOwner() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders/1/cancel")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido cancelado com sucesso"));

        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    void cancelOrder_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders/1/cancel")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    private OrderResponse createOrderResponse(Long id, String userEmail, OrderStatus status) {
        return OrderResponse.builder()
                .id(id)
                .userEmail(userEmail)
                .userName("Test User")
                .totalAmount(BigDecimal.valueOf(199.99))
                .status(status)
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(List.of(
                        OrderResponse.OrderItemResponse.builder()
                                .productId(1L)
                                .productName("Test Product")
                                .quantity(2)
                                .unitPrice(BigDecimal.valueOf(99.99))
                                .totalPrice(BigDecimal.valueOf(199.98))
                                .build()
                ))
                .build();
    }
}