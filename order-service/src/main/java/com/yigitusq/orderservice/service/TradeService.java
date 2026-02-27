package com.yigitusq.orderservice.service;

import com.yigitusq.orderservice.entity.Order;
import com.yigitusq.orderservice.entity.User;
import com.yigitusq.orderservice.repository.OrderRepository;
import com.yigitusq.orderservice.repository.UserRepository;
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

    @Transactional
    public String buyAsset(Long userId, String symbol, BigDecimal amount, BigDecimal currentPrice) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        BigDecimal totalCost = currentPrice.multiply(amount);

        if (user.getBalance().compareTo(totalCost) < 0) {
            log.warn("Yetersiz bakiye! Kullanıcı: {}, Gereken: {}", user.getUsername(), totalCost);
            return "Bakiye yetersiz!";
        }

        user.setBalance(user.getBalance().subtract(totalCost));
        userRepository.save(user);

        // Emri kaydet
        Order order = new Order();
        order.setUserId(userId);
        order.setSymbol(symbol);
        order.setPrice(currentPrice);
        order.setQuantity(amount);
        order.setSide("BUY");
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("İşlem Başarılı! {} kullanıcısı {} fiyattan {} adet {} aldı.",
                user.getUsername(), currentPrice, amount, symbol);

        return "Alım işlemi başarıyla gerçekleşti. Kalan bakiye: " + user.getBalance();
    }
}