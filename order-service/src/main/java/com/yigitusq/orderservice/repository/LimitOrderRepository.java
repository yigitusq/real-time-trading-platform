package com.yigitusq.orderservice.repository;

import com.yigitusq.orderservice.entity.LimitOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;

public interface LimitOrderRepository extends JpaRepository<LimitOrder, Long> {

    // Alış emri için: Hedef fiyat >= Piyasa fiyatı
    List<LimitOrder> findBySymbolAndStatusAndSideAndTargetPriceGreaterThanEqual(
            String symbol, String status, String side, BigDecimal currentPrice);

    // Satış emri için: Hedef fiyat <= Piyasa fiyatı
    List<LimitOrder> findBySymbolAndStatusAndSideAndTargetPriceLessThanEqual(
            String symbol, String status, String side, BigDecimal currentPrice);
}