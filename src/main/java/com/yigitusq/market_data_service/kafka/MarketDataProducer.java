package com.yigitusq.market_data_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "market-prices";

    public void sendRawPriceTick(String symbol, String rawJsonPayload) {
        kafkaTemplate.send(TOPIC, symbol, rawJsonPayload);
    }
}