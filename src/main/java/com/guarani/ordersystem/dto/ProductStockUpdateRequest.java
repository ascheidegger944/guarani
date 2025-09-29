package com.guarani.ordersystem.dto;

import com.guarani.ordersystem.entity.enums.StockMovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockUpdateRequest {

    @NotNull(message = "Tipo de movimentação é obrigatório")
    private StockMovementType movementType;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser positiva")
    private Integer quantity;

    private String reason;
}