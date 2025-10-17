package com.guarani.repository;

import com.guarani.model.Order;
import com.guarani.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.finalAmount BETWEEN :minAmount AND :maxAmount")
    List<Order> findByFinalAmountBetween(@Param("minAmount") BigDecimal minAmount,
                                         @Param("maxAmount") BigDecimal maxAmount);
}