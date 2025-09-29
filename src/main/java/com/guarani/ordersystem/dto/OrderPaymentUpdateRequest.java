package com.guarani.ordersystem.dto;

import com.guarani.ordersystem.entity.enums.PaymentMethod;
import com.guarani.ordersystem.entity.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaymentUpdateRequest {

    @NotNull(message = "Status do pagamento é obrigatório")
    private PaymentStatus paymentStatus;

    private PaymentMethod paymentMethod;

    private String transactionId;
}