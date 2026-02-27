package com.yigitusq.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.yigitusq.orderservice.model.BinanceTradeMessage;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketConsumerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TradeService tradeService; // TradeService'i buraya çağırdık

    // Test için hedef fiyatı şimdilik buraya yazıyoruz (Örn: BTC 66.000$'ın altına inerse al)
    private final BigDecimal targetPrice = new BigDecimal("66000.00");
    private boolean isBought = false;

    @KafkaListener(topics = "market-prices", groupId = "order-service-group")
    public void consumeMarketData(String message) {
        try {
            // 1. ADIM: Gelen mesaj eğer çift tırnakla başlıyorsa (metin içine gömülmüşse)
            // önce içindeki asıl metni çıkarıyoruz.
            String jsonToParse = message;
            if (message.startsWith("\"") && message.endsWith("\"")) {
                // objectMapper.readTree(message).asText() metni dıştaki tırnaklardan kurtarır.
                jsonToParse = objectMapper.readTree(message).asText();
            }

            // 2. ADIM: Şimdi asıl JSON objesini modelimize çeviriyoruz.
            BinanceTradeMessage trade = objectMapper.readValue(jsonToParse, BinanceTradeMessage.class);

            // KRİTİK KONTROL: Eğer teknik bir mesajsa (boşsa) pas geç
            if (trade.getSymbol() == null || trade.getPrice() == null) {
                return;
            }

            BigDecimal currentPrice = new BigDecimal(trade.getPrice());
            String symbol = trade.getSymbol();

            // Otomatik emir mantığı devam ediyor...
            if (currentPrice.compareTo(targetPrice) < 0 && !isBought) {
                log.info("OTOMATİK ALIM TETİKLENDİ! Fiyat: {} $", currentPrice);
                tradeService.buyAsset(1L, symbol, new BigDecimal("0.01"), currentPrice);
                isBought = true;
            }

        } catch (Exception e) {
            log.error("Mesaj işlenirken hata oluştu! Mesaj: {} | Hata: {}", message, e.getMessage());
        }
    }
}