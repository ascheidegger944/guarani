package com.guarani.ordersystem.repository;

import com.guarani.ordersystem.entity.Order;
import com.guarani.ordersystem.entity.OrderItem;
import com.guarani.ordersystem.entity.Product;
import com.guarani.ordersystem.entity.User;
import com.guarani.ordersystem.entity.enums.OrderStatus;
import com.guarani.ordersystem.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.test.database.replace=NONE",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User user1;
    private User user2;
    private Product product1;
    private Product product2;
    private Order order1;
    private Order order2;
    private Order order3;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create users
        user1 = User.builder()
                .name("John Doe")
                .email("john@email.com")
                .password("password123")
                .roles(Set.of(Role.CLIENT))
                .build();

        user2 = User.builder()
                .name("Jane Smith")
                .email("jane@email.com")
                .password("password456")
                .roles(Set.of(Role.CLIENT))
                .build();

        user1 = entityManager.persistAndFlush(user1);
        user2 = entityManager.persistAndFlush(user2);

        // Create products
        product1 = Product.builder()
                .name("Laptop Dell")
                .description("High performance laptop")
                .price(BigDecimal.valueOf(2999.99))
                .category("INFORMATICA")
                .stockQuantity(15)
                .active(true)
                .build();

        product2 = Product.builder()
                .name("Smartphone Samsung")
                .description("Latest smartphone")
                .price(BigDecimal.valueOf(1999.99))
                .category("ELETRONICOS")
                .stockQuantity(8)
                .active(true)
                .build();

        product1 = entityManager.persistAndFlush(product1);
        product2 = entityManager.persistAndFlush(product2);

        // Create orders
        order1 = Order.builder()
                .user(user1)
                .totalAmount(BigDecimal.valueOf(4999.98))
                .status(OrderStatus.PENDING)
                .build();

        order2 = Order.builder()
                .user(user1)
                .totalAmount(BigDecimal.valueOf(1999.99))
                .status(OrderStatus.CONFIRMED)
                .build();

        order3 = Order.builder()
                .user(user2)
                .totalAmount(BigDecimal.valueOf(2999.99))
                .status(OrderStatus.DELIVERED)
                .build();

        // Add order items
        OrderItem item1 = OrderItem.builder()
                .order(order1)
                .product(product1)
                .quantity(1)
                .unitPrice(product1.getPrice())
                .totalPrice(product1.getPrice())
                .build();

        OrderItem item2 = OrderItem.builder()
                .order(order1)
                .product(product2)
                .quantity(1)
                .unitPrice(product2.getPrice())
                .totalPrice(product2.getPrice())
                .build();

        OrderItem item3 = OrderItem.builder()
                .order(order2)
                .product(product2)
                .quantity(1)
                .unitPrice(product2.getPrice())
                .totalPrice(product2.getPrice())
                .build();

        OrderItem item4 = OrderItem.builder()
                .order(order3)
                .product(product1)
                .quantity(1)
                .unitPrice(product1.getPrice())
                .totalPrice(product1.getPrice())
                .build();

        order1.getItems().add(item1);
        order1.getItems().add(item2);
        order2.getItems().add(item3);
        order3.getItems().add(item4);

        // Persist orders
        order1 = entityManager.persistAndFlush(order1);
        order2 = entityManager.persistAndFlush(order2);
        order3 = entityManager.persistAndFlush(order3);
    }

    @Test
    void findByUserEmail_ShouldReturnUserOrders() {
        // Act
        Page<Order> page = orderRepository.findByUserEmail("john@email.com", PageRequest.of(0, 10));

        // Assert
        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(order -> order.getUser().getEmail().equals("john@email.com")));
        assertTrue(page.getContent().stream().anyMatch(order -> order.getStatus() == OrderStatus.PENDING));
        assertTrue(page.getContent().stream().anyMatch(order -> order.getStatus() == OrderStatus.CONFIRMED));
    }

    @Test
    void findByUserEmail_ShouldReturnEmpty_WhenUserHasNoOrders() {
        // Arrange
        User newUser = User.builder()
                .name("New User")
                .email("new@email.com")
                .password("password")
                .roles(Set.of(Role.CLIENT))
                .build();
        entityManager.persistAndFlush(newUser);

        // Act
        Page<Order> page = orderRepository.findByUserEmail("new@email.com", PageRequest.of(0, 10));

        // Assert
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void findByUserEmail_ShouldReturnEmpty_WhenUserNotExists() {
        // Act
        Page<Order> page = orderRepository.findByUserEmail("nonexistent@email.com", PageRequest.of(0, 10));

        // Assert
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void findByStatus_ShouldReturnOrdersWithStatus() {
        // Act
        Page<Order> pendingPage = orderRepository.findByStatus(OrderStatus.PENDING, PageRequest.of(0, 10));
        Page<Order> confirmedPage = orderRepository.findByStatus(OrderStatus.CONFIRMED, PageRequest.of(0, 10));
        Page<Order> deliveredPage = orderRepository.findByStatus(OrderStatus.DELIVERED, PageRequest.of(0, 10));

        // Assert
        assertEquals(1, pendingPage.getTotalElements());
        assertEquals(OrderStatus.PENDING, pendingPage.getContent().get(0).getStatus());

        assertEquals(1, confirmedPage.getTotalElements());
        assertEquals(OrderStatus.CONFIRMED, confirmedPage.getContent().get(0).getStatus());

        assertEquals(1, deliveredPage.getTotalElements());
        assertEquals(OrderStatus.DELIVERED, deliveredPage.getContent().get(0).getStatus());
    }

    @Test
    void findByStatus_ShouldReturnEmpty_WhenNoOrdersWithStatus() {
        // Act
        Page<Order> page = orderRepository.findByStatus(OrderStatus.CANCELLED, PageRequest.of(0, 10));

        // Assert
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void save_ShouldPersistOrder_WithAllFieldsAndItems() {
        // Arrange
        User newUser = User.builder()
                .name("Test User")
                .email("test@email.com")
                .password("password")
                .roles(Set.of(Role.CLIENT))
                .build();
        newUser = entityManager.persistAndFlush(newUser);

        Order newOrder = Order.builder()
                .user(newUser)
                .totalAmount(BigDecimal.valueOf(3999.99))
                .status(OrderStatus.PENDING)
                .build();

        OrderItem newItem = OrderItem.builder()
                .order(newOrder)
                .product(product1)
                .quantity(2)
                .unitPrice(product1.getPrice())
                .totalPrice(product1.getPrice().multiply(BigDecimal.valueOf(2)))
                .build();

        newOrder.getItems().add(newItem);

        // Act
        Order savedOrder = orderRepository.save(newOrder);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertNotNull(savedOrder.getId());
        assertEquals(newUser.getId(), savedOrder.getUser().getId());
        assertEquals(BigDecimal.valueOf(3999.99), savedOrder.getTotalAmount());
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(1, savedOrder.getItems().size());
        assertEquals(2, savedOrder.getItems().get(0).getQuantity());
        assertEquals(product1.getId(), savedOrder.getItems().get(0).getProduct().getId());

        // Verify can be retrieved with items
        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getId());
        assertTrue(retrievedOrder.isPresent());
        assertEquals(1, retrievedOrder.get().getItems().size());
        assertEquals("Laptop Dell", retrievedOrder.get().getItems().get(0).getProduct().getName());
    }

    @Test
    void updateOrder_ShouldUpdateFields() {
        // Arrange
        Order order = orderRepository.findById(order1.getId()).get();
        order.setStatus(OrderStatus.PROCESSING);
        order.setTotalAmount(BigDecimal.valueOf(5999.99));

        // Act
        Order updatedOrder = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Order> retrievedOrder = orderRepository.findById(updatedOrder.getId());
        assertTrue(retrievedOrder.isPresent());
        assertEquals(OrderStatus.PROCESSING, retrievedOrder.get().getStatus());
        assertEquals(BigDecimal.valueOf(5999.99), retrievedOrder.get().getTotalAmount());
    }

    @Test
    void deleteOrder_ShouldRemoveOrderAndItems() {
        // Arrange
        Long orderId = order1.getId();

        // Act
        orderRepository.deleteById(orderId);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Order> deletedOrder = orderRepository.findById(orderId);
        assertFalse(deletedOrder.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        // Act
        List<Order> allOrders = orderRepository.findAll();

        // Assert
        assertEquals(3, allOrders.size());
    }

    @Test
    void findById_ShouldReturnOrder_WhenIdExists() {
        // Act
        Optional<Order> result = orderRepository.findById(order1.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("john@email.com", result.get().getUser().getEmail());
        assertEquals(2, result.get().getItems().size());
        assertEquals(OrderStatus.PENDING, result.get().getStatus());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdNotExists() {
        // Act
        Optional<Order> result = orderRepository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByUserEmail_WithPageable_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<Order> page = orderRepository.findByUserEmail("john@email.com", pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalPages());
        assertTrue(page.getContent().stream().allMatch(order -> order.getUser().getEmail().equals("john@email.com")));
    }

    @Test
    void findByStatus_WithPageable_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<Order> page = orderRepository.findByStatus(OrderStatus.PENDING, pageable);

        // Assert
        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals(1, page.getTotalPages());
        assertEquals(OrderStatus.PENDING, page.getContent().get(0).getStatus());
    }

    @Test
    void orderCreation_ShouldSetAuditFields() {
        // Arrange
        User newUser = User.builder()
                .name("Audit User")
                .email("audit@email.com")
                .password("password")
                .roles(Set.of(Role.CLIENT))
                .build();
        newUser = entityManager.persistAndFlush(newUser);

        Order newOrder = Order.builder()
                .user(newUser)
                .totalAmount(BigDecimal.valueOf(1999.99))
                .status(OrderStatus.PENDING)
                .build();

        // Act
        Order savedOrder = orderRepository.save(newOrder);
        entityManager.flush();

        // Assert
        assertNotNull(savedOrder.getCreatedAt());
        assertNotNull(savedOrder.getUpdatedAt());
    }

    @Test
    void findByUserEmail_ShouldOrderByCreatedAtDesc_ByDefault() {
        // Act
        Page<Order> page = orderRepository.findByUserEmail("john@email.com", PageRequest.of(0, 10));
        List<Order> orders = page.getContent();

        // Assert - Should be ordered by createdAt descending (newest first)
        if (orders.size() > 1) {
            assertTrue(orders.get(0).getCreatedAt().isAfter(orders.get(1).getCreatedAt()) ||
                    orders.get(0).getCreatedAt().isEqual(orders.get(1).getCreatedAt()));
        }
    }

    @Test
    void findByUserEmail_ShouldWorkWithDifferentEmailCases() {
        // Act
        Page<Order> result1 = orderRepository.findByUserEmail("JOHN@EMAIL.COM", PageRequest.of(0, 10));
        Page<Order> result2 = orderRepository.findByUserEmail("john@email.com", PageRequest.of(0, 10));

        // Assert
        assertEquals(2, result1.getTotalElements());
        assertEquals(2, result2.getTotalElements());
    }

    @Test
    void save_ShouldHandleOrder_WithMultipleItems() {
        // Arrange
        User newUser = User.builder()
                .name("Multi Item User")
                .email("multi@email.com")
                .password("password")
                .roles(Set.of(Role.CLIENT))
                .build();
        newUser = entityManager.persistAndFlush(newUser);

        Order newOrder = Order.builder()
                .user(newUser)
                .totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        // Add multiple items
        for (int i = 0; i < 5; i++) {
            OrderItem item = OrderItem.builder()
                    .order(newOrder)
                    .product(product1)
                    .quantity(i + 1)
                    .unitPrice(product1.getPrice())
                    .totalPrice(product1.getPrice().multiply(BigDecimal.valueOf(i + 1)))
                    .build();
            newOrder.getItems().add(item);
        }

        // Recalculate total
        newOrder.recalculateTotalAmount();

        // Act
        Order savedOrder = orderRepository.save(newOrder);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getId());
        assertTrue(retrievedOrder.isPresent());
        assertEquals(5, retrievedOrder.get().getItems().size());
        assertEquals(BigDecimal.valueOf(2999.99 * 15), retrievedOrder.get().getTotalAmount()); // 1+2+3+4+5 = 15 * price
    }
}