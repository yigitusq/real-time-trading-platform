package com.yigitusq.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Duration;
// import java.util.concurrent.ConcurrentHashMap; // was using instead of redis

@Service
@Slf4j
@RequiredArgsConstructor
public class PriceService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PRICE_KEY_PREFIX = "price:";
    private static final Duration PRICE_TTL = Duration.ofSeconds(5);

    public void updatePrice(String symbol, BigDecimal price) {
        try {
            redisTemplate.opsForValue().set(
                    PRICE_KEY_PREFIX + symbol,
                    price.toPlainString(),
                    PRICE_TTL
            );
        } catch (Exception e) {
            log.warn("Redis'e fiyat yazılamadı: {} → {}", symbol, e.getMessage());
        }
    }

    public BigDecimal getPrice(String symbol) {
        try {
            String price = redisTemplate.opsForValue()
                    .get(PRICE_KEY_PREFIX + symbol.toUpperCase());
            if (price != null) {
                return new BigDecimal(price);
            }
        } catch (Exception e) {
            log.warn("Redis'ten fiyat okunamadı: {} → {}", symbol, e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}