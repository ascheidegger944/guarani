package com.guarani.ordersystem.repository;

import com.guarani.ordersystem.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.test.database.replace=NONE",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;
    private Product product3;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

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

        product3 = Product.builder()
                .name("Tablet Apple")
                .description("iPad 10th generation")
                .price(BigDecimal.valueOf(4299.99))
                .category("ELETRONICOS")
                .stockQuantity(5)
                .active(true)
                .build();

        inactiveProduct = Product.builder()
                .name("Old Product")
                .description("Discontinued product")
                .price(BigDecimal.valueOf(499.99))
                .category("ELETRONICOS")
                .stockQuantity(0)
                .active(false)
                .build();

        // Persist products
        product1 = entityManager.persistAndFlush(product1);
        product2 = entityManager.persistAndFlush(product2);
        product3 = entityManager.persistAndFlush(product3);
        inactiveProduct = entityManager.persistAndFlush(inactiveProduct);
    }

    @Test
    void findByActiveTrue_ShouldReturnOnlyActiveProducts() {
        // Act
        List<Product> activeProducts = productRepository.findByActiveTrue();

        // Assert
        assertEquals(3, activeProducts.size());
        assertTrue(activeProducts.stream().allMatch(Product::getActive));
        assertTrue(activeProducts.stream().anyMatch(p -> p.getName().equals("Laptop Dell")));
        assertTrue(activeProducts.stream().anyMatch(p -> p.getName().equals("Smartphone Samsung")));
        assertTrue(activeProducts.stream().anyMatch(p -> p.getName().equals("Tablet Apple")));
        assertFalse(activeProducts.stream().anyMatch(p -> p.getName().equals("Old Product")));
    }

    @Test
    void findByCategoryIgnoreCase_ShouldReturnProductsInCategory() {
        // Act
        List<Product> electronics = productRepository.findByCategoryIgnoreCase("ELETRONICOS");
        List<Product> informatics = productRepository.findByCategoryIgnoreCase("INFORMATICA");

        // Assert
        assertEquals(2, electronics.size());
        assertTrue(electronics.stream().allMatch(p -> p.getCategory().equalsIgnoreCase("ELETRONICOS")));
        assertTrue(electronics.stream().anyMatch(p -> p.getName().equals("Smartphone Samsung")));
        assertTrue(electronics.stream().anyMatch(p -> p.getName().equals("Tablet Apple")));

        assertEquals(1, informatics.size());
        assertEquals("Laptop Dell", informatics.get(0).getName());
    }

    @Test
    void findByCategoryIgnoreCase_ShouldBeCaseInsensitive() {
        // Act
        List<Product> result1 = productRepository.findByCategoryIgnoreCase("eletronicos");
        List<Product> result2 = productRepository.findByCategoryIgnoreCase("ELETRONICOS");
        List<Product> result3 = productRepository.findByCategoryIgnoreCase("Eletronicos");

        // Assert
        assertEquals(2, result1.size());
        assertEquals(2, result2.size());
        assertEquals(2, result3.size());
    }

    @Test
    void findByCategoryIgnoreCase_ShouldReturnEmpty_WhenCategoryNotExists() {
        // Act
        List<Product> result = productRepository.findByCategoryIgnoreCase("NONEXISTENT");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findByStockQuantityLessThanAndActiveTrue_ShouldReturnLowStockProducts() {
        // Act
        List<Product> lowStockProducts = productRepository.findByStockQuantityLessThanAndActiveTrue(10);

        // Assert
        assertEquals(2, lowStockProducts.size()); // Samsung (8) and Apple (5)
        assertTrue(lowStockProducts.stream().allMatch(p -> p.getStockQuantity() < 10));
        assertTrue(lowStockProducts.stream().allMatch(Product::getActive));
        assertTrue(lowStockProducts.stream().anyMatch(p -> p.getName().equals("Smartphone Samsung")));
        assertTrue(lowStockProducts.stream().anyMatch(p -> p.getName().equals("Tablet Apple")));
    }

    @Test
    void findByStockQuantityLessThanAndActiveTrue_ShouldNotIncludeInactiveProducts() {
        // Act
        List<Product> lowStockProducts = productRepository.findByStockQuantityLessThanAndActiveTrue(10);

        // Assert
        assertTrue(lowStockProducts.stream().noneMatch(p -> p.getName().equals("Old Product")));
    }

    @Test
    void save_ShouldPersistProduct_WithAllFields() {
        // Arrange
        Product newProduct = Product.builder()
                .name("New Product")
                .description("New description")
                .price(BigDecimal.valueOf(1499.99))
                .category("INFORMATICA")
                .stockQuantity(20)
                .active(true)
                .build();

        // Act
        Product savedProduct = productRepository.save(newProduct);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertNotNull(savedProduct.getId());
        assertEquals("New Product", savedProduct.getName());
        assertEquals("New description", savedProduct.getDescription());
        assertEquals(BigDecimal.valueOf(1499.99), savedProduct.getPrice());
        assertEquals("INFORMATICA", savedProduct.getCategory());
        assertEquals(20, savedProduct.getStockQuantity());
        assertTrue(savedProduct.getActive());

        // Verify can be retrieved
        Optional<Product> retrievedProduct = productRepository.findById(savedProduct.getId());
        assertTrue(retrievedProduct.isPresent());
        assertEquals("New Product", retrievedProduct.get().getName());
    }

    @Test
    void updateProduct_ShouldUpdateFields() {
        // Arrange
        Product product = productRepository.findById(product1.getId()).get();
        product.setName("Updated Laptop");
        product.setPrice(BigDecimal.valueOf(3499.99));
        product.setStockQuantity(25);

        // Act
        Product updatedProduct = productRepository.save(product);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Product> retrievedProduct = productRepository.findById(updatedProduct.getId());
        assertTrue(retrievedProduct.isPresent());
        assertEquals("Updated Laptop", retrievedProduct.get().getName());
        assertEquals(BigDecimal.valueOf(3499.99), retrievedProduct.get().getPrice());
        assertEquals(25, retrievedProduct.get().getStockQuantity());
    }

    @Test
    void deleteProduct_ShouldRemoveProduct() {
        // Arrange
        Long productId = product1.getId();

        // Act
        productRepository.deleteById(productId);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertFalse(deletedProduct.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllProducts() {
        // Act
        List<Product> allProducts = productRepository.findAll();

        // Assert
        assertEquals(4, allProducts.size()); // Including inactive
    }

    @Test
    void findById_ShouldReturnProduct_WhenIdExists() {
        // Act
        Optional<Product> result = productRepository.findById(product1.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Laptop Dell", result.get().getName());
        assertEquals("INFORMATICA", result.get().getCategory());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdNotExists() {
        // Act
        Optional<Product> result = productRepository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByActiveTrue_WithPageable_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Product> page = productRepository.findByActiveTrue(pageable);

        // Assert
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
        assertTrue(page.getContent().stream().allMatch(Product::getActive));
    }

    @Test
    void findByCategoryIgnoreCase_WithPageable_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<Product> page = productRepository.findByCategoryIgnoreCase("ELETRONICOS", pageable);

        // Assert
        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalPages());
        assertTrue(page.getContent().stream().allMatch(p -> p.getCategory().equalsIgnoreCase("ELETRONICOS")));
    }

    @Test
    void productCreation_ShouldSetAuditFields() {
        // Arrange
        Product newProduct = Product.builder()
                .name("Audit Product")
                .description("Test audit")
                .price(BigDecimal.valueOf(999.99))
                .category("TEST")
                .stockQuantity(10)
                .active(true)
                .build();

        // Act
        Product savedProduct = productRepository.save(newProduct);
        entityManager.flush();

        // Assert
        assertNotNull(savedProduct.getCreatedAt());
        assertNotNull(savedProduct.getUpdatedAt());
    }

    @Test
    void findByActiveTrue_ShouldOrderByName_WhenUsingSort() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("name"));

        // Act
        Page<Product> page = productRepository.findByActiveTrue(pageable);
        List<Product> products = page.getContent();

        // Assert - Should be ordered by name
        assertTrue(products.get(0).getName().compareTo(products.get(1).getName()) <= 0);
        assertTrue(products.get(1).getName().compareTo(products.get(2).getName()) <= 0);
    }

    @Test
    void searchWithSpecification_ShouldFilterByName() {
        // Arrange
        Specification<Product> spec = (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%laptop%");

        // Act
        List<Product> results = productRepository.findAll(spec);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Laptop Dell", results.get(0).getName());
    }

    @Test
    void searchWithSpecification_ShouldFilterByCategory() {
        // Arrange
        Specification<Product> spec = (root, query, cb) ->
                cb.equal(cb.lower(root.get("category")), "informatica");

        // Act
        List<Product> results = productRepository.findAll(spec);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Laptop Dell", results.get(0).getName());
    }

    @Test
    void searchWithSpecification_ShouldFilterByPriceRange() {
        // Arrange
        Specification<Product> spec = (root, query, cb) ->
                cb.between(root.get("price"), BigDecimal.valueOf(2000), BigDecimal.valueOf(3000));

        // Act
        List<Product> results = productRepository.findAll(spec);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Laptop Dell", results.get(0).getName());
    }
}