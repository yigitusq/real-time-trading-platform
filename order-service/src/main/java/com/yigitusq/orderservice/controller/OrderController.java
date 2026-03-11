package com.yigitusq.orderservice.controller;

import com.yigitusq.orderservice.entity.LimitOrder;
import com.yigitusq.orderservice.entity.TradeHistory;
import com.yigitusq.orderservice.repository.LimitOrderRepository;
import com.yigitusq.orderservice.repository.TradeHistoryRepository;
import com.yigitusq.orderservice.service.TradeService;
import lombok.*;
import org.springframework.web.bind.annotation.*;
import com.yigitusq.orderservice.dto.PortfolioResponse;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final TradeService tradeService;
    private final LimitOrderRepository limitOrderRepository;
    private final TradeHistoryRepository tradeHistoryRepository;

    @PostMapping("/buy")
    public String buy(@RequestParam Long userId,
                      @RequestParam String symbol,
                      @RequestParam BigDecimal amount,
                      @RequestParam BigDecimal price) {
        return tradeService.buyAsset(userId, symbol, amount, price);
    }

    @PostMapping("/limit")
    public LimitOrder createLimitOrder(@RequestBody LimitOrder order) {
        order.setOrderType("LIMIT");
        order.setStatus("PENDING");
        return limitOrderRepository.save(order);
    }

    @PostMapping("/sell")
    public String sell(@RequestParam Long userId,
                       @RequestParam String symbol,
                       @RequestParam BigDecimal amount,
                       @RequestParam BigDecimal price) {
        return tradeService.sellAsset(userId, symbol, amount, price);
    }

    @PostMapping("/stop-loss")
    public LimitOrder createStopLoss(@RequestParam Long userId,
                                     @RequestParam String symbol,
                                     @RequestParam BigDecimal quantity,
                                     @RequestParam BigDecimal stopPrice) {
        LimitOrder order = new LimitOrder();
        order.setUserId(userId);
        order.setSymbol(symbol);
        order.setQuantity(quantity);
        order.setStopPrice(stopPrice);
        order.setTargetPrice(stopPrice); // satış piyasa fiyatından olur
        order.setSide("SELL");
        order.setOrderType("STOP_LOSS");
        order.setStatus("PENDING");
        return limitOrderRepository.save(order);
    }

    @PostMapping("/take-profit")
    public LimitOrder createTakeProfit(@RequestParam Long userId,
                                       @RequestParam String symbol,
                                       @RequestParam BigDecimal quantity,
                                       @RequestParam BigDecimal targetPrice) {
        LimitOrder order = new LimitOrder();
        order.setUserId(userId);
        order.setSymbol(symbol);
        order.setQuantity(quantity);
        order.setTargetPrice(targetPrice);
        order.setSide("SELL");
        order.setOrderType("TAKE_PROFIT");
        order.setStatus("PENDING");
        return limitOrderRepository.save(order);
    }

    @GetMapping("/portfolio/{userId}")
    public PortfolioResponse getPortfolio(@PathVariable Long userId) {
        return tradeService.getPortfolio(userId);
    }

    @GetMapping("/history/{userId}")
    public List<TradeHistory> getTradeHistory(@PathVariable Long userId) {
        return tradeHistoryRepository.findAllByUserIdOrderByTimestampDesc(userId);
    }
}