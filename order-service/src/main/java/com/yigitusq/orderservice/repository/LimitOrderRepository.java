package com.yigitusq.orderservice.repository;

import com.yigitusq.orderservice.entity.LimitOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;

public interface LimitOrderRepository extends JpaRepository<LimitOrder, Long> {

    List<LimitOrder> findBySymbolAndStatusAndSideAndOrderTypeAndTargetPriceGreaterThanEqual(
            String symbol, String status, String side, String orderType, BigDecimal currentPrice);

    List<LimitOrder> findBySymbolAndStatusAndSideAndOrderTypeAndTargetPriceLessThanEqual(
            String symbol, String status, String side, String orderType, BigDecimal currentPrice);

    List<LimitOrder> findBySymbolAndStatusAndOrderType(
            String symbol, String status, String orderType);
}