package com.guarani.ordersystem.controller;

import com.guarani.ordersystem.dto.*;
import com.guarani.ordersystem.service.ProductService;
import com.guarani.ordersystem.util.Constants;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "APIs para gerenciamento de produtos")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Retorna lista paginada de produtos ativos")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponse> products = productService.findActiveProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(products)));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar produtos", description = "Busca produtos com filtros avançados")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<ProductResponse> products = productService.searchProducts(name, category, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(products)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter produto por ID", description = "Retorna produto específico por ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Produto encontrado", product));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Listar produtos por categoria", description = "Retorna produtos de categoria específica")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<ProductResponse> products = productService.findByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(products)));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Listar produtos com estoque baixo", description = "Retorna produtos com estoque menor que 10 unidades")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts() {
        List<ProductResponse> products = productService.findLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success("Produtos com estoque baixo", products));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Criar produto", description = "Cria um novo produto no sistema")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse product = productService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Produto criado com sucesso", product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar produto", description = "Atualiza dados de um produto existente")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse product = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Produto atualizado com sucesso", product));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar estoque", description = "Atualiza estoque do produto com movimentação")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody ProductStockUpdateRequest request
    ) {
        ProductResponse product = productService.updateStock(id, request.getQuantity(),
                request.getMovementType(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Estoque atualizado com sucesso", product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Excluir produto", description = "Desativa produto do sistema (soft delete)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Produto excluído com sucesso"));
    }
}