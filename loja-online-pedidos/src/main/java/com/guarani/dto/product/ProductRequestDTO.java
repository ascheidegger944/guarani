package com.guarani.dto.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Integer stockQuantity;
}