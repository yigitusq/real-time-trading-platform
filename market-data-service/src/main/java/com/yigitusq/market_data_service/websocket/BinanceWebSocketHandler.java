package com.yigitusq.market_data_service.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yigitusq.market_data_service.model.BinanceTradeMessage;
import com.yigitusq.market_data_service.kafka.MarketDataProducer; // İçe aktarmayı unutma!
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MarketDataProducer producer;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        BinanceTradeMessage tradeMessage = objectMapper.readValue(payload, BinanceTradeMessage.class);

        log.info("Konsol: Sembol: {} | Fiyat: {} $", tradeMessage.getSymbol(), tradeMessage.getPrice());

        producer.sendRawPriceTick(tradeMessage.getSymbol(), payload);
    }
}