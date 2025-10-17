package com.guarani.dto.order;

import com.guarani.model.Order;
import com.guarani.model.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequestDTO {
    private List<OrderItemRequestDTO> items;
    private BigDecimal discount;
    private BigDecimal shippingFee;
    private PaymentMethod paymentMethod;
}