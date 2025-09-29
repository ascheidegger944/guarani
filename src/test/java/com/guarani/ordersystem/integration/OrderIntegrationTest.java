package com.guarani.ordersystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarani.ordersystem.dto.OrderRequest;
import com.guarani.ordersystem.dto.OrderResponse;
import com.guarani.ordersystem.entity.*;
import com.guarani.ordersystem.entity.enums.OrderStatus;
import com.guarani.ordersystem.entity.enums.Role;
import com.guarani.ordersystem.repository.OrderRepository;
import com.guarani.ordersystem.repository.ProductRepository;
import com.guarani.ordersystem.repository.UserRepository;
import com.guarani.ordersystem.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtService jwtService;

    private String clientToken;
    private String operatorToken;
    private String adminToken;
    private User clientUser;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        clientUser = User.builder()
                .name("Client User")
                .email("client@email.com")
                .password("password")
                .roles(Set.of(Role.CLIENT))
                .build();
        userRepository.save(clientUser);
        clientToken = jwtService.generateToken(clientUser);

        User operator = User.builder()
                .name("Operator User")
                .email("operator@email.com")
                .password("password")
                .roles(Set.of(Role.OPERATOR))
                .build();
        userRepository.save(operator);
        operatorToken = jwtService.generateToken(operator);

        User admin = User.builder()
                .name("Admin User")
                .email("admin@email.com")
                .password("password")
                .roles(Set.of(Role.ADMIN))
                .build();
        userRepository.save(admin);
        adminToken = jwtService.generateToken(admin);

        // Create test products
        product1 = Product.builder()
                .name("Product 1")
                .description("Test Product 1")
                .price(BigDecimal.valueOf(100.0))
                .category("ELETRONICOS")
                .stockQuantity(50)
                .active(true)
                .build();

        product2 = Product.builder()
                .name("Product 2")
                .description("Test Product 2")
                .price(BigDecimal.valueOf(200.0))
                .category("INFORMATICA")
                .stockQuantity(30)
                .active(true)
                .build();

        productRepository.saveAll(Set.of(product1, product2));
    }

    @Test
    void createOrder_ShouldCreateOrderAndUpdateStock() throws Exception {
        // Arrange
        OrderRequest.OrderItemRequest item1 = OrderRequest.OrderItemRequest.builder()
                .productId(product1.getId())
                .quantity(2)
                .build();

        OrderRequest.OrderItemRequest item2 = OrderRequest.OrderItemRequest.builder()
                .productId(product2.getId())
                .quantity(1)
                .build();

        OrderRequest request = OrderRequest.builder()
                .items(Set.of(item1, item2))
                .build();

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + clientToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido criado com sucesso"))
                .andExpect(jsonPath("$.data.userEmail").value("client@email.com"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(400.0)) // 2*100 + 1*200
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andReturn();

        // Verify order was saved in database
        String responseContent = result.getResponse().getContentAsString();
        OrderResponse orderResponse = objectMapper.readValue(
                objectMapper.readTree(responseContent).get("data").toString(),
                OrderResponse.class
        );

        Order savedOrder = orderRepository.findById(orderResponse.getId()).orElse(null);
        assertNotNull(savedOrder);
        assertEquals(clientUser.getId(), savedOrder.getUser().getId());
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(BigDecimal.valueOf(400.0), savedOrder.getTotalAmount());
        assertEquals(2, savedOrder.getItems().size());

        // Verify stock was updated
        Product updatedProduct1 = productRepository.findById(product1.getId()).orElse(null);
        Product updatedProduct2 = productRepository.findById(product2.getId()).orElse(null);

        assertNotNull(updatedProduct1);
        assertNotNull(updatedProduct2);
        assertEquals(48, updatedProduct1.getStockQuantity()); // 50 - 2
        assertEquals(29, updatedProduct2.getStockQuantity()); // 30 - 1
    }

    @Test
    void createOrder_ShouldReturnBadRequest_WhenInsufficientStock() throws Exception {
        // Arrange
        OrderRequest.OrderItemRequest item = OrderRequest.OrderItemRequest.builder()
                .productId(product1.getId())
                .quantity(100) // More than available stock (50)
                .build();

        OrderRequest request = OrderRequest.builder()
                .items(Set.of(item))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + clientToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para o produto: Product 1"));
    }

    @Test
    void createOrder_ShouldReturnBadRequest_WhenProductNotActive() throws Exception {
        // Arrange - Deactivate product
        product1.setActive(false);
        productRepository.save(product1);

        OrderRequest.OrderItemRequest item = OrderRequest.OrderItemRequest.builder()
                .productId(product1.getId())
                .quantity(1)
                .build();

        OrderRequest request = OrderRequest.builder()
                .items(Set.of(item))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + clientToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Produto não está disponível: Product 1"));
    }

    @Test
    void getOrders_ShouldReturnUserOrders_WhenClient() throws Exception {
        // Arrange - Create test orders
        createTestOrder(clientUser, OrderStatus.PENDING);
        createTestOrder(clientUser, OrderStatus.CONFIRMED);

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + clientToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].userEmail").value("client@email.com"))
                .andExpect(jsonPath("$.data.content[1].userEmail").value("client@email.com"));
    }

    @Test
    void getOrders_ShouldReturnAllOrders_WhenAdmin() throws Exception {
        // Arrange - Create test orders for different users
        User otherUser = User.builder()
                .name("Other User")
                .email("other@email.com")
                .password("password")
                .roles(Set.of(Role.CLIENT))
                .build();
        userRepository.save(otherUser);

        createTestOrder(clientUser, OrderStatus.PENDING);
        createTestOrder(otherUser, OrderStatus.CONFIRMED);

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenOwner() throws Exception {
        // Arrange
        Order order = createTestOrder(clientUser, OrderStatus.PENDING);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{id}", order.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(order.getId()))
                .andExpect(jsonPath("$.data.userEmail").value("client@email.com"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void getOrderById_ShouldReturnForbidden_WhenNotOwner() throws Exception {
        // Arrange
        User otherUser = User.builder()
                .name("Other User")
                .email("other@email.com")
                .password("password")
                .roles(Set.of(Role.CLIENT))
                .build();
        userRepository.save(otherUser);

        Order order = createTestOrder(otherUser, OrderStatus.PENDING);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{id}", order.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso negado para visualizar este pedido"));
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus_WhenOperator() throws Exception {
        // Arrange
        Order order = createTestOrder(clientUser, OrderStatus.PENDING);

        String statusUpdateJson = """
            {
                "status": "CONFIRMED"
            }
            """;

        // Act & Assert
        mockMvc.perform(patch("/api/orders/{id}/status", order.getId())
                        .header("Authorization", "Bearer " + operatorToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Status do pedido atualizado"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        // Verify status was updated in database
        Order updatedOrder = orderRepository.findById(order.getId()).orElse(null);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.CONFIRMED, updatedOrder.getStatus());
    }

    @Test
    void cancelOrder_ShouldCancelOrderAndRestoreStock() throws Exception {
        // Arrange
        Order order = createTestOrder(clientUser, OrderStatus.PENDING);
        int initialStock = product1.getStockQuantity();

        // Act & Assert
        mockMvc.perform(post("/api/orders/{id}/cancel", order.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido cancelado com sucesso"));

        // Verify order was cancelled
        Order cancelledOrder = orderRepository.findById(order.getId()).orElse(null);
        assertNotNull(cancelledOrder);
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());

        // Verify stock was restored
        Product restoredProduct = productRepository.findById(product1.getId()).orElse(null);
        assertNotNull(restoredProduct);
        assertEquals(initialStock, restoredProduct.getStockQuantity()); // Stock should be restored
    }

    @Test
    void cancelOrder_ShouldReturnError_WhenOrderCannotBeCancelled() throws Exception {
        // Arrange
        Order order = createTestOrder(clientUser, OrderStatus.DELIVERED); // Already delivered

        // Act & Assert
        mockMvc.perform(post("/api/orders/{id}/cancel", order.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Pedido não pode ser cancelado no status atual: DELIVERED"));
    }

    private Order createTestOrder(User user, OrderStatus status) {
        Order order = Order.builder()
                .user(user)
                .totalAmount(BigDecimal.valueOf(100.0))
                .status(status)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product1)
                .quantity(1)
                .unitPrice(product1.getPrice())
                .totalPrice(product1.getPrice())
                .build();

        order.getItems().add(orderItem);
        return orderRepository.save(order);
    }
}