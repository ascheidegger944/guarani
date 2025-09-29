package com.guarani.ordersystem.service;

import com.guarani.ordersystem.dto.ProductRequest;
import com.guarani.ordersystem.dto.ProductResponse;
import com.guarani.ordersystem.entity.Product;
import com.guarani.ordersystem.entity.ProductPriceHistory;
import com.guarani.ordersystem.entity.StockMovement;
import com.guarani.ordersystem.entity.enums.StockMovementType;
import com.guarani.ordersystem.exception.BusinessException;
import com.guarani.ordersystem.exception.ResourceNotFoundException;
import com.guarani.ordersystem.repository.ProductRepository;
import com.guarani.ordersystem.util.Constants;
import com.guarani.ordersystem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "products")
public class ProductService {

    private final ProductRepository productRepository;

    @Cacheable(key = "#id")
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        log.info("Buscando produto por ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
        return mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(Pageable pageable) {
        log.info("Buscando todos os produtos paginados");
        return productRepository.findAll(pageable)
                .map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findByCategory(String category, Pageable pageable) {
        log.info("Buscando produtos por categoria: {}", category);
        return productRepository.findByCategoryIgnoreCase(category, pageable)
                .map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findActiveProducts(Pageable pageable) {
        log.info("Buscando produtos ativos");
        return productRepository.findByActiveTrue(pageable)
                .map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, String category, BigDecimal minPrice,
                                                BigDecimal maxPrice, Pageable pageable) {
        log.info("Buscando produtos com filtros - nome: {}, categoria: {}, preço: {}-{}",
                name, category, minPrice, maxPrice);

        Specification<Product> spec = Specification.where(null);

        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }

        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        spec = spec.and((root, query, cb) -> cb.isTrue(root.get("active")));

        return productRepository.findAll(spec, pageable)
                .map(this::mapToProductResponse);
    }

    @CacheEvict(allEntries = true)
    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("Criando novo produto: {}", request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .active(true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Produto criado com ID: {}", savedProduct.getId());

        return mapToProductResponse(savedProduct);
    }

    @CacheEvict(key = "#id")
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        log.info("Atualizando produto ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));

        // Registrar histórico de preço se o preço mudou
        if (request.getPrice() != null && !request.getPrice().equals(product.getPrice())) {
            ProductPriceHistory priceHistory = ProductPriceHistory.builder()
                    .product(product)
                    .oldPrice(product.getPrice())
                    .newPrice(request.getPrice())
                    .changedBy(SecurityUtils.getCurrentUsername())
                    .build();
            product.getPriceHistory().add(priceHistory);
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    @CacheEvict(key = "#id")
    @Transactional
    public void delete(Long id) {
        log.info("Deletando produto ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));

        product.setActive(false);
        productRepository.save(product);
    }

    @CacheEvict(key = "#productId")
    @Transactional
    public ProductResponse updateStock(Long productId, Integer quantity, StockMovementType movementType, String reason) {
        log.info("Atualizando estoque do produto ID: {} - tipo: {}, quantidade: {}",
                productId, movementType, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", productId));

        int previousStock = product.getStockQuantity();
        int newStock = previousStock;

        switch (movementType) {
            case ENTRADA:
                newStock = previousStock + quantity;
                break;
            case SAIDA:
                if (previousStock < quantity) {
                    throw new BusinessException("Estoque insuficiente para saída");
                }
                newStock = previousStock - quantity;
                break;
            case AJUSTE:
                if (quantity < 0) {
                    throw new BusinessException("Quantidade de ajuste não pode ser negativa");
                }
                newStock = quantity;
                break;
        }

        // Registrar movimentação de estoque
        StockMovement stockMovement = StockMovement.builder()
                .product(product)
                .movementType(movementType)
                .quantity(quantity)
                .previousStock(previousStock)
                .newStock(newStock)
                .reason(reason)
                .createdBy(SecurityUtils.getCurrentUsername())
                .build();
        product.getStockMovements().add(stockMovement);

        product.setStockQuantity(newStock);
        Product updatedProduct = productRepository.save(product);

        return mapToProductResponse(updatedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findLowStockProducts() {
        log.info("Buscando produtos com estoque baixo");
        return productRepository.findByStockQuantityLessThanAndActiveTrue(10)
                .stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}