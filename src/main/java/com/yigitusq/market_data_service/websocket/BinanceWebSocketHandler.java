package com.yigitusq.market_data_service.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yigitusq.market_data_service.model.BinanceTradeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class BinanceWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        BinanceTradeMessage tradeMessage = objectMapper.readValue(payload, BinanceTradeMessage.class);

        log.info("Yeni İşlem Yakalandı! Sembol: {} | Fiyat: {} $ | Miktar: {}",
                tradeMessage.getSymbol(), tradeMessage.getPrice(), tradeMessage.getQuantity());
    }
}