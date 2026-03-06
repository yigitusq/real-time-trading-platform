package com.yigitusq.orderservice.controller;

import com.yigitusq.orderservice.entity.LimitOrder;
import com.yigitusq.orderservice.repository.LimitOrderRepository;
import com.yigitusq.orderservice.service.TradeService;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final TradeService tradeService;
    private final LimitOrderRepository limitOrderRepository; // HATA BURADAYDI, BUNU EKLE

    @PostMapping("/buy")
    public String buy(@RequestParam Long userId,
                      @RequestParam String symbol,
                      @RequestParam BigDecimal amount,
                      @RequestParam BigDecimal price) {
        return tradeService.buyAsset(userId, symbol, amount, price);
    }

    @PostMapping("/limit")
    public LimitOrder createLimitOrder(@RequestBody LimitOrder order) {
        order.setStatus("PENDING");
        return limitOrderRepository.save(order);
    }
}