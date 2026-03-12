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
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketConsumerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TradeService tradeService;
    private final LimitOrderRepository limitOrderRepository;
    private final PriceService priceService;

    // YENİ — sembol başına son kontrol zamanını tut
    private final ConcurrentHashMap<String, Long> lastCheckTime = new ConcurrentHashMap<>();
    private static final long CHECK_INTERVAL_MS = 1000; // 1 saniyede bir kontrol et

    @KafkaListener(topics = "market-prices", groupId = "order-service-group")
    public void consumeMarketData(String message) {
        try {
            String jsonToParse = message.startsWith("\"")
                    ? objectMapper.readTree(message).asText() : message;
            BinanceTradeMessage trade = objectMapper.readValue(jsonToParse, BinanceTradeMessage.class);

            if (trade.getSymbol() == null || trade.getPrice() == null) return;

            BigDecimal currentPrice = new BigDecimal(trade.getPrice());
            String symbol = trade.getSymbol();

            // Fiyatı her tick'te güncelle (cache — DB yok)
            priceService.updatePrice(symbol, currentPrice);

            // DB sorgusunu throttle et — 1 saniyede bir yap
            long now = System.currentTimeMillis();
            Long last = lastCheckTime.get(symbol);
            if (last != null && (now - last) < CHECK_INTERVAL_MS) {
                return; // Henüz 1 saniye geçmedi, DB'ye gitme
            }
            lastCheckTime.put(symbol, now);

            // --- Limit emirleri ---
            List<LimitOrder> pendingBuyOrders = limitOrderRepository
                    .findBySymbolAndStatusAndSideAndOrderTypeAndTargetPriceGreaterThanEqual(
                            symbol, "PENDING", "BUY", "LIMIT", currentPrice);
            pendingBuyOrders.forEach(o -> executeLimitOrder(o, currentPrice));

            List<LimitOrder> pendingSellOrders = limitOrderRepository
                    .findBySymbolAndStatusAndSideAndOrderTypeAndTargetPriceLessThanEqual(
                            symbol, "PENDING", "SELL", "LIMIT", currentPrice);
            pendingSellOrders.forEach(o -> executeLimitOrder(o, currentPrice));

            // --- Stop-Loss ---
            List<LimitOrder> stopLossOrders = limitOrderRepository
                    .findBySymbolAndStatusAndOrderType(symbol, "PENDING", "STOP_LOSS");
            stopLossOrders.stream()
                    .filter(o -> currentPrice.compareTo(o.getStopPrice()) <= 0)
                    .forEach(o -> executeLimitOrder(o, currentPrice));

            // --- Take-Profit ---
            List<LimitOrder> takeProfitOrders = limitOrderRepository
                    .findBySymbolAndStatusAndOrderType(symbol, "PENDING", "TAKE_PROFIT");
            takeProfitOrders.stream()
                    .filter(o -> currentPrice.compareTo(o.getTargetPrice()) >= 0)
                    .forEach(o -> executeLimitOrder(o, currentPrice));

        } catch (Exception e) {
            log.error("Kafka işleme hatası: {}", e.getMessage());
        }
    }

    private void executeLimitOrder(LimitOrder order, BigDecimal currentPrice) {
        try {
            // İşlemi dene
            if ("BUY".equals(order.getSide())) {
                tradeService.buyAsset(order.getUserId(), order.getSymbol(), order.getQuantity(), currentPrice);
            } else {
                tradeService.sellAsset(order.getUserId(), order.getSymbol(), order.getQuantity(), currentPrice);
            }

            // Exception fırlamadıysa işlem BAŞARILI demektir!
            order.setStatus("COMPLETED");
            limitOrderRepository.save(order);
            log.info("Emir Gerçekleşti! ID: {} | Tip: {} | Gerçekleşen Fiyat: {}", order.getId(), order.getOrderType(), currentPrice);

        } catch (com.yigitusq.orderservice.exception.TradeException e) {
            order.setStatus("FAILED");
            limitOrderRepository.save(order);
            log.warn("Emir İptal Edildi (Yetersiz Bakiye/Varlık)! ID: {} | Sebep: {}", order.getId(), e.getMessage());

        } catch (Exception e) {
            log.error("Limit emir işlenirken beklenmeyen sistem hatası! Emir ID: {}", order.getId(), e);
        }
    }
}