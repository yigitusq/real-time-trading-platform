package com.yigitusq.orderservice.service;

import com.fasterxml.jackson.databind.*;
import com.yigitusq.orderservice.entity.LimitOrder;
import com.yigitusq.orderservice.model.BinanceTradeMessage;
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

            // 1. ALIŞ (BUY) Emirlerini Kontrol Et: Hedef Fiyat >= Piyasa Fiyatı
            List<LimitOrder> pendingBuyOrders = limitOrderRepository
                    .findBySymbolAndStatusAndSideAndTargetPriceGreaterThanEqual(symbol, "PENDING", "BUY", currentPrice);

            for (LimitOrder order : pendingBuyOrders) {
                executeLimitOrder(order, currentPrice);
            }

            // 2. SATIŞ (SELL) Emirlerini Kontrol Et: Hedef Fiyat <= Piyasa Fiyatı
            List<LimitOrder> pendingSellOrders = limitOrderRepository
                    .findBySymbolAndStatusAndSideAndTargetPriceLessThanEqual(symbol, "PENDING", "SELL", currentPrice);

            for (LimitOrder order : pendingSellOrders) {
                executeLimitOrder(order, currentPrice);
            }

        } catch (Exception e) {
            log.error("Kafka işleme hatası: {}", e.getMessage());
        }
    }

    private void executeLimitOrder(LimitOrder order, BigDecimal currentPrice) {
        try {
            String result;
            if ("BUY".equals(order.getSide())) {
                result = tradeService.buyAsset(order.getUserId(), order.getSymbol(), order.getQuantity(), currentPrice);
            } else {
                result = tradeService.sellAsset(order.getUserId(), order.getSymbol(), order.getQuantity(), currentPrice);
            }

            if (result.contains("başarılı")) {
                order.setStatus("COMPLETED");
                limitOrderRepository.save(order);
                log.info("Limit Emir Gerçekleşti! ID: {} | Tip: {} | Gerçekleşen Fiyat: {}", order.getId(), order.getSide(), currentPrice);
            } else {
                log.warn("Emir işlenemedi! ID: {} | Sebep: {}", order.getId(), result);
            }
        } catch (Exception e) {
            log.error("Limit emir işlenirken beklenmeyen hata! Emir ID: {}", order.getId(), e);
        }
    }
}