package com.yigitusq.orderservice.controller;

import com.yigitusq.orderservice.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final TradeService tradeService;

    @PostMapping("/buy")
    public String buy(@RequestParam Long userId,
                      @RequestParam String symbol,
                      @RequestParam BigDecimal amount,
                      @RequestParam BigDecimal price) {
        return tradeService.buyAsset(userId, symbol, amount, price);
    }
}