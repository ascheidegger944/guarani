package com.guarani.ordersystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @NotEmpty(message = "Pedido deve conter pelo menos um item")
    private List<@Valid OrderItemRequest> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemRequest {

        @NotNull(message = "ID do produto é obrigatório")
        private Long productId;

        @NotNull(message = "Quantidade é obrigatória")
        private Integer quantity;
    }
}