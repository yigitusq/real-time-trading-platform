package com.yigitusq.orderservice.repository;

import com.yigitusq.orderservice.entity.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {
    List<TradeHistory> findAllByUserIdOrderByTimestampDesc(Long userId);
}