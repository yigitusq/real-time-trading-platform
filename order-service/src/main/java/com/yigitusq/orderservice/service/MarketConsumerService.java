package com.yigitusq.orderservice.service;

import com.fasterxml.jackson.databind.*;
import com.yigitusq.orderservice.entity.LimitOrder;
import com.yigitusq.orderservice.model.BinanceTradeMessage; // Binance modelin burada mı kontrol et
import com.yigitusq.orderservice.repository.LimitOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketConsumerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TradeService tradeService;
    private final LimitOrderRepository limitOrderRepository;

    @KafkaListener(topics = "market-prices", groupId = "order-service-group")
    public void consumeMarketData(String message) {
        try {
            String jsonToParse = message.startsWith("\"") ? objectMapper.readTree(message).asText() : message;
            BinanceTradeMessage trade = objectMapper.readValue(jsonToParse, BinanceTradeMessage.class);

            if (trade.getSymbol() == null || trade.getPrice() == null) return;

            BigDecimal currentPrice = new BigDecimal(trade.getPrice());
            String symbol = trade.getSymbol();

            List<LimitOrder> pendingBuyOrders = limitOrderRepository
                    .findBySymbolAndStatusAndSideAndTargetPriceGreaterThanEqual(symbol, "PENDING", "BUY", currentPrice);

            for (LimitOrder order : pendingBuyOrders) {
                log.info("Limit Emir Eşleşti! Emir ID: {}", order.getId());
                tradeService.buyAsset(order.getUserId(), symbol, order.getQuantity(), currentPrice);
                order.setStatus("COMPLETED");
                limitOrderRepository.save(order);
            }

        } catch (Exception e) {
            log.error("Kafka işleme hatası: {}", e.getMessage());
        }
    }
}