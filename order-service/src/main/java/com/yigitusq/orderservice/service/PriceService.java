package com.yigitusq.orderservice.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PriceService {
    // Fiyatları RAM'de tutmak için
    private final ConcurrentHashMap<String, BigDecimal> priceCache = new ConcurrentHashMap<>();

    public void updatePrice(String symbol, BigDecimal price) {
        priceCache.put(symbol, price);
    }

    public BigDecimal getPrice(String symbol) {
        return priceCache.getOrDefault(symbol, BigDecimal.ZERO);
    }
}