package com.guarani.ordersystem.service;

import com.guarani.ordersystem.dto.ProductRequest;
import com.guarani.ordersystem.dto.ProductResponse;
import com.guarani.ordersystem.entity.Product;
import com.guarani.ordersystem.exception.ResourceNotFoundException;
import com.guarani.ordersystem.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findById_ShouldReturnProduct_WhenProductExists() {
        // Arrange
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .category("ELETRONICOS")
                .stockQuantity(10)
                .active(true)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        ProductResponse result = productService.findById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void findById_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.findById(productId));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void create_ShouldSaveAndReturnProduct_WhenValidRequest() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("New Product")
                .description("Product Description")
                .price(BigDecimal.valueOf(149.99))
                .category("ELETRONICOS")
                .stockQuantity(20)
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("New Product")
                .description("Product Description")
                .price(BigDecimal.valueOf(149.99))
                .category("ELETRONICOS")
                .stockQuantity(20)
                .active(true)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        ProductResponse result = productService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Product", result.getName());
        assertEquals(BigDecimal.valueOf(149.99), result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void findAll_ShouldReturnPageOfProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .category("ELETRONICOS")
                .build();

        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // Act
        Page<ProductResponse> result = productService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Product", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void searchProducts_ShouldReturnFilteredProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .category("ELETRONICOS")
                .active(true)
                .build();

        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);

        // Act
        Page<ProductResponse> result = productService.searchProducts(
                "Test", "ELETRONICOS", BigDecimal.valueOf(50), BigDecimal.valueOf(100), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void delete_ShouldDeactivateProduct() {
        // Arrange
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .name("Test Product")
                .active(true)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        productService.delete(productId);

        // Assert
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
        assertFalse(product.getActive()); // Product should be deactivated
    }
}