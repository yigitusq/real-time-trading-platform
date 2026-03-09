package com.yigitusq.orderservice.service;

import com.yigitusq.orderservice.entity.*;
import com.yigitusq.orderservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    @Transactional
    public String buyAsset(Long userId, String symbol, BigDecimal amount, BigDecimal currentPrice) {
        User user = userRepository.findById(userId).orElseThrow();
        BigDecimal totalCost = currentPrice.multiply(amount);

        if (user.getBalance().compareTo(totalCost) < 0) return "Bakiye yetersiz!";

        // 1. Nakit bakiyeyi düş
        user.setBalance(user.getBalance().subtract(totalCost));
        userRepository.save(user);

        // 2. Coin varlığını ekle veya güncelle
        Asset asset = assetRepository.findByUserIdAndSymbol(userId, symbol)
                .orElse(new Asset());
        asset.setUserId(userId);
        asset.setSymbol(symbol);

        BigDecimal currentQty = asset.getQuantity() != null ? asset.getQuantity() : BigDecimal.ZERO;
        asset.setQuantity(currentQty.add(amount));
        assetRepository.save(asset);

        saveOrder(userId, symbol, amount, currentPrice, "BUY");
        return "Alım başarılı. Yeni bakiye: " + user.getBalance();
    }

    @Transactional
    public String sellAsset(Long userId, String symbol, BigDecimal amount, BigDecimal currentPrice) {
        // 1. Kullanıcının elinde bu coin var mı bak
        Asset asset = assetRepository.findByUserIdAndSymbol(userId, symbol)
                .orElseThrow(() -> new RuntimeException("Bu varlığa sahip değilsiniz!"));

        if (asset.getQuantity().compareTo(amount) < 0) return "Yetersiz varlık miktarı!";

        // 2. Coin miktarını düş
        asset.setQuantity(asset.getQuantity().subtract(amount));
        assetRepository.save(asset);

        // 3. Nakit bakiyeyi artır (Satıştan gelen para)
        User user = userRepository.findById(userId).orElseThrow();
        BigDecimal totalGain = currentPrice.multiply(amount);
        user.setBalance(user.getBalance().add(totalGain));
        userRepository.save(user);

        saveOrder(userId, symbol, amount, currentPrice, "SELL");
        return "Satış başarılı. Yeni bakiye: " + user.getBalance();
    }

    private void saveOrder(Long userId, String symbol, BigDecimal amount, BigDecimal price, String side) {
        Order order = new Order();
        order.setUserId(userId);
        order.setSymbol(symbol);
        order.setPrice(price);
        order.setQuantity(amount);
        order.setSide(side);
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}