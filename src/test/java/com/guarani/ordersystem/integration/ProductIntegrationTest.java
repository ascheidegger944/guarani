package com.guarani.ordersystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarani.ordersystem.dto.ProductRequest;
import com.guarani.ordersystem.dto.ProductResponse;
import com.guarani.ordersystem.entity.Product;
import com.guarani.ordersystem.entity.User;
import com.guarani.ordersystem.entity.enums.Role;
import com.guarani.ordersystem.repository.ProductRepository;
import com.guarani.ordersystem.repository.UserRepository;
import com.guarani.ordersystem.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String operatorToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
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
        Product product1 = Product.builder()
                .name("Laptop Dell")
                .description("High performance laptop")
                .price(BigDecimal.valueOf(2999.99))
                .category("INFORMATICA")
                .stockQuantity(15)
                .active(true)
                .build();

        Product product2 = Product.builder()
                .name("Smartphone Samsung")
                .description("Latest smartphone")
                .price(BigDecimal.valueOf(1999.99))
                .category("ELETRONICOS")
                .stockQuantity(8)
                .active(true)
                .build();

        Product inactiveProduct = Product.builder()
                .name("Old Product")
                .description("Discontinued product")
                .price(BigDecimal.valueOf(499.99))
                .category("ELETRONICOS")
                .stockQuantity(0)
                .active(false)
                .build();

        productRepository.saveAll(Set.of(product1, product2, inactiveProduct));
    }

    @Test
    void getAllProducts_ShouldReturnOnlyActiveProducts() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2)) // Only 2 active products
                .andExpect(jsonPath("$.data.content[0].name").exists())
                .andExpect(jsonPath("$.data.content[1].name").exists());
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() throws Exception {
        // Arrange
        Product product = productRepository.findAll().get(0);

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(product.getId()))
                .andExpect(jsonPath("$.data.name").value(product.getName()))
                .andExpect(jsonPath("$.data.price").value(product.getPrice().doubleValue()))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void getProductById_ShouldReturnNotFound_WhenProductNotExists() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void searchProducts_ShouldReturnFilteredProducts() throws Exception {
        // Act & Assert - Search by category
        mockMvc.perform(get("/api/products/search")
                        .param("category", "INFORMATICA")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].category").value("INFORMATICA"));

        // Act & Assert - Search by name
        mockMvc.perform(get("/api/products/search")
                        .param("name", "Samsung")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Smartphone Samsung"));
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/category/ELETRONICOS")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].category").value("ELETRONICOS"))
                .andExpect(jsonPath("$.data.content[0].name").value("Smartphone Samsung"));
    }

    @Test
    void createProduct_ShouldCreateProduct_WhenAuthenticatedAsOperator() throws Exception {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("New Gaming Monitor")
                .description("27 inch gaming monitor")
                .price(BigDecimal.valueOf(1599.99))
                .category("INFORMATICA")
                .stockQuantity(25)
                .build();

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + operatorToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Produto criado com sucesso"))
                .andExpect(jsonPath("$.data.name").value("New Gaming Monitor"))
                .andExpect(jsonPath("$.data.price").value(1599.99))
                .andExpect(jsonPath("$.data.stockQuantity").value(25))
                .andExpect(jsonPath("$.data.active").value(true))
                .andReturn();

        // Verify product was saved in database
        String responseContent = result.getResponse().getContentAsString();
        ProductResponse productResponse = objectMapper.readValue(
                objectMapper.readTree(responseContent).get("data").toString(),
                ProductResponse.class
        );

        Product savedProduct = productRepository.findById(productResponse.getId()).orElse(null);
        assertNotNull(savedProduct);
        assertEquals("New Gaming Monitor", savedProduct.getName());
        assertEquals(BigDecimal.valueOf(1599.99), savedProduct.getPrice());
        assertEquals(25, savedProduct.getStockQuantity());
        assertTrue(savedProduct.getActive());
    }

    @Test
    void createProduct_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("New Product")
                .price(BigDecimal.valueOf(999.99))
                .category("ELETRONICOS")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProduct_ShouldUpdateProduct_WhenAuthenticatedAsOperator() throws Exception {
        // Arrange
        Product existingProduct = productRepository.findAll().get(0);
        ProductRequest request = ProductRequest.builder()
                .name("Updated Product Name")
                .description("Updated description")
                .price(BigDecimal.valueOf(2499.99))
                .category("INFORMATICA")
                .stockQuantity(30)
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/products/{id}", existingProduct.getId())
                        .header("Authorization", "Bearer " + operatorToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Produto atualizado com sucesso"))
                .andExpect(jsonPath("$.data.name").value("Updated Product Name"))
                .andExpect(jsonPath("$.data.price").value(2499.99))
                .andExpect(jsonPath("$.data.stockQuantity").value(30));

        // Verify product was updated in database
        Product updatedProduct = productRepository.findById(existingProduct.getId()).orElse(null);
        assertNotNull(updatedProduct);
        assertEquals("Updated Product Name", updatedProduct.getName());
        assertEquals(BigDecimal.valueOf(2499.99), updatedProduct.getPrice());
        assertEquals(30, updatedProduct.getStockQuantity());
    }

    @Test
    void deleteProduct_ShouldDeactivateProduct_WhenAuthenticatedAsAdmin() throws Exception {
        // Arrange
        Product product = productRepository.findAll().get(0);

        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", product.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Produto excluído com sucesso"));

        // Verify product was deactivated in database
        Product deletedProduct = productRepository.findById(product.getId()).orElse(null);
        assertNotNull(deletedProduct);
        assertFalse(deletedProduct.getActive());
    }

    @Test
    void getLowStockProducts_ShouldReturnLowStockProducts_WhenAuthenticated() throws Exception {
        // Arrange - Create a low stock product
        Product lowStockProduct = Product.builder()
                .name("Low Stock Item")
                .price(BigDecimal.valueOf(99.99))
                .category("ELETRONICOS")
                .stockQuantity(3) // Low stock
                .active(true)
                .build();
        productRepository.save(lowStockProduct);

        // Act & Assert
        mockMvc.perform(get("/api/products/low-stock")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Low Stock Item"))
                .andExpect(jsonPath("$.data[0].stockQuantity").value(3));
    }
}