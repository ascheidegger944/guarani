package com.guarani.ordersystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarani.ordersystem.dto.*;
import com.guarani.ordersystem.entity.enums.StockMovementType;
import com.guarani.ordersystem.exception.ResourceNotFoundException;
import com.guarani.ordersystem.service.ProductService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void getAllProducts_ShouldReturnProducts_WhenNoAuthRequired() throws Exception {
        // Arrange
        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .category("ELETRONICOS")
                .stockQuantity(10)
                .active(true)
                .build();

        Page<ProductResponse> page = new PageImpl<>(List.of(product));
        when(productService.findActiveProducts(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.data.content[0].price").value(99.99))
                .andExpect(jsonPath("$.data.content[0].category").value("ELETRONICOS"));
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() throws Exception {
        // Arrange
        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .category("ELETRONICOS")
                .stockQuantity(10)
                .active(true)
                .build();

        when(productService.findById(1L)).thenReturn(product);

        // Act & Assert
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Produto encontrado"))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.price").value(99.99));
    }

    @Test
    void getProductById_ShouldReturnNotFound_WhenProductNotExists() throws Exception {
        // Arrange
        when(productService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Produto", "id", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void searchProducts_ShouldReturnFilteredProducts() throws Exception {
        // Arrange
        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .category("ELETRONICOS")
                .stockQuantity(10)
                .active(true)
                .build();

        Page<ProductResponse> page = new PageImpl<>(List.of(product));
        when(productService.searchProducts(anyString(), anyString(), any(), any(), any()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                        .param("name", "Test")
                        .param("category", "ELETRONICOS")
                        .param("minPrice", "50")
                        .param("maxPrice", "100")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"));
    }

    @Test
    void getProductsByCategory_ShouldReturnProducts() throws Exception {
        // Arrange
        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .category("ELETRONICOS")
                .stockQuantity(10)
                .active(true)
                .build();

        Page<ProductResponse> page = new PageImpl<>(List.of(product));
        when(productService.findByCategory(eq("ELETRONICOS"), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/products/category/ELETRONICOS")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].category").value("ELETRONICOS"));
    }

    @Test
    @WithMockUser(roles = {"OPERATOR"})
    void getLowStockProducts_ShouldReturnProducts_WhenAuthenticated() throws Exception {
        // Arrange
        ProductResponse product = ProductResponse.builder()
                .id(1L)
                .name("Low Stock Product")
                .price(BigDecimal.valueOf(99.99))
                .category("ELETRONICOS")
                .stockQuantity(5) // Low stock
                .active(true)
                .build();

        when(productService.findLowStockProducts()).thenReturn(List.of(product));

        // Act & Assert
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Produtos com estoque baixo"))
                .andExpect(jsonPath("$.data[0].name").value("Low Stock Product"))
                .andExpect(jsonPath("$.data[0].stockQuantity").value(5));
    }

    @Test
    void getLowStockProducts_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"OPERATOR"})
    void createProduct_ShouldReturnProduct_WhenValidRequestAndAuthenticated() throws Exception {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("New Product")
                .description("Product Description")
                .price(BigDecimal.valueOf(149.99))
                .category("ELETRONICOS")
                .stockQuantity(20)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("New Product")
                .description("Product Description")
                .price(BigDecimal.valueOf(149.99))
                .category("ELETRONICOS")
                .stockQuantity(20)
                .active(true)
                .build();

        when(productService.create(any(ProductRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Produto criado com sucesso"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("New Product"))
                .andExpect(jsonPath("$.data.price").value(149.99));
    }

    @Test
    void createProduct_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("New Product")
                .price(BigDecimal.valueOf(149.99))
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
    @WithMockUser(roles = {"OPERATOR"})
    void createProduct_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("")  // Nome vazio
                .price(BigDecimal.valueOf(-10))  // Preço negativo
                .category("")  // Categoria vazia
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = {"OPERATOR"})
    void updateProduct_ShouldReturnUpdatedProduct_WhenValidRequest() throws Exception {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(199.99))
                .category("INFORMATICA")
                .stockQuantity(15)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(199.99))
                .category("INFORMATICA")
                .stockQuantity(15)
                .active(true)
                .build();

        when(productService.update(eq(1L), any(ProductRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/products/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Produto atualizado com sucesso"))
                .andExpect(jsonPath("$.data.name").value("Updated Product"))
                .andExpect(jsonPath("$.data.category").value("INFORMATICA"));
    }

    @Test
    @WithMockUser(roles = {"OPERATOR"})
    void updateStock_ShouldUpdateStock_WhenValidRequest() throws Exception {
        // Arrange
        ProductStockUpdateRequest request = ProductStockUpdateRequest.builder()
                .movementType(StockMovementType.ENTRADA)
                .quantity(10)
                .reason("Reposição de estoque")
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .category("ELETRONICOS")
                .stockQuantity(20) // Updated stock
                .active(true)
                .build();

        when(productService.updateStock(eq(1L), eq(10), eq(StockMovementType.ENTRADA), anyString()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/products/1/stock")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estoque atualizado com sucesso"))
                .andExpect(jsonPath("$.data.stockQuantity").value(20));
    }

    @Test
    @WithMockUser(roles = {"OPERATOR"})
    void deleteProduct_ShouldDeactivateProduct() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/products/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Produto excluído com sucesso"));

        verify(productService, times(1)).delete(1L);
    }

    @Test
    void deleteProduct_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/products/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(productService, never()).delete(anyLong());
    }
}