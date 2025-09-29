package com.guarani.ordersystem.entity;

import com.guarani.ordersystem.entity.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 50)
    private StockMovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "previous_stock", nullable = false)
    private Integer previousStock;

    @Column(name = "new_stock", nullable = false)
    private Integer newStock;

    @Column(length = 255)
    private String reason;

    @Column(name = "movement_date", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime movementDate;

    @Column(name = "created_by")
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (movementDate == null) {
            movementDate = LocalDateTime.now();
        }
    }

    public Integer getStockVariation() {
        return newStock - previousStock;
    }
}