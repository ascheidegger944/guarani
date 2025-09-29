package com.guarani.ordersystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome não pode exceder 255 caracteres")
    private String name;

    @Size(max = 1000, message = "Descrição não pode exceder 1000 caracteres")
    private String description;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal price;

    @NotBlank(message = "Categoria é obrigatória")
    @Size(max = 100, message = "Categoria não pode exceder 100 caracteres")
    private String category;

    private Integer stockQuantity;

    private Boolean active;
}