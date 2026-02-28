package com.yigitusq.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String symbol;      // Ex: BTCUSDT
    private BigDecimal price;   
    private BigDecimal quantity;
    private String side;        // BUY or SELL
    private LocalDateTime createdAt;
}
