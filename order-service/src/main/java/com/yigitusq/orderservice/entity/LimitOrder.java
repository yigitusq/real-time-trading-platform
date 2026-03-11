package com.yigitusq.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "limit_orders")
@Data
public class LimitOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String symbol;      // BTCUSDT
    private BigDecimal targetPrice;
    private BigDecimal quantity;
    private String side;        // BUY veya SELL
    private String status;      // PENDING, COMPLETED
    private String orderType; // LIMIT, STOP_LOSS, TAKE_PROFIT
    private BigDecimal stopPrice;
}
