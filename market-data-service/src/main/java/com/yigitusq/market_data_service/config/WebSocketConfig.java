package com.yigitusq.market_data_service.config; // Kendi paket ismine göre düzenle

import com.yigitusq.market_data_service.websocket.BinanceWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Configuration
public class WebSocketConfig {

    @Value("${binance.websocket.url:wss://stream.binance.com:9443/ws/btcusdt@trade}")
    private String binanceUrl;

    @Bean
    public WebSocketConnectionManager connectionManager(BinanceWebSocketHandler handler) {
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketConnectionManager manager = new WebSocketConnectionManager(client, handler, binanceUrl);
        manager.setAutoStartup(true);
        return manager;
    }
}