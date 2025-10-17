package com.guarani.dto.order;

import com.guarani.model.OrderStatus;
import com.guarani.model.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private List<OrderItemDTO> items;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}