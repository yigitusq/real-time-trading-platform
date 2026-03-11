package com.yigitusq.orderservice.service;

import com.yigitusq.orderservice.dto.PortfolioResponse;
import com.yigitusq.orderservice.entity.*;
import com.yigitusq.orderservice.exception.TradeException;
import com.yigitusq.orderservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final PriceService priceService;
    private final TradeHistoryRepository tradeHistoryRepository;

    @Transactional
    public String buyAsset(Long userId, String symbol, BigDecimal amount, BigDecimal currentPrice) {
        User user = userRepository.findById(userId).orElseThrow();
        BigDecimal totalCost = currentPrice.multiply(amount);


        if (user.getBalance().compareTo(totalCost) < 0) {
            throw new TradeException("Yetersiz bakiye! İşlem tutarı: " + totalCost + "$, Mevcut nakit: " + user.getBalance() + "$");
        }

        // 1. Nakit bakiyeyi düş
        user.setBalance(user.getBalance().subtract(totalCost));
        userRepository.save(user);

        // 2. Coin varlığını ekle veya güncelle
        Asset asset = assetRepository.findByUserIdAndSymbol(userId, symbol)
                .orElse(new Asset());
        asset.setUserId(userId);
        asset.setSymbol(symbol);

        BigDecimal currentQty = asset.getQuantity() != null ? asset.getQuantity() : BigDecimal.ZERO;
        BigDecimal currentAvgCost = asset.getAverageCost() != null ? asset.getAverageCost() : BigDecimal.ZERO;
        // Ağırlıklı ortalama formülü:
        // newAvg = ((mevcutMiktar * mevcutOrtalama) + (yeniMiktar * yeniFiyat)) / (mevcutMiktar + yeniMiktar)
        BigDecimal newQty = currentQty.add(amount);
        BigDecimal newAvgCost = (currentQty.multiply(currentAvgCost))
                .add(amount.multiply(currentPrice))
                .divide(newQty, 8, RoundingMode.HALF_UP);

        asset.setQuantity(newQty);
        asset.setAverageCost(newAvgCost);
        assetRepository.save(asset);

        saveOrder(userId, symbol, amount, currentPrice, "BUY");
        // İşlem Geçmişini (Makbuz) Kaydet
        TradeHistory history = new TradeHistory();
        history.setUserId(userId);
        history.setSymbol(symbol);
        history.setSide("BUY");
        history.setQuantity(amount);
        history.setPrice(currentPrice);
        history.setTotalAmount(totalCost);
        history.setTimestamp(java.time.LocalDateTime.now());
        tradeHistoryRepository.save(history);
        return "Alım başarılı. Yeni bakiye: " + user.getBalance();
    }

    @Transactional
    public String sellAsset(Long userId, String symbol, BigDecimal amount, BigDecimal currentPrice) {
        // 1. Kullanıcının elinde bu coin var mı bak
        Asset asset = assetRepository.findByUserIdAndSymbol(userId, symbol)
                .orElseThrow(() -> new TradeException("Cüzdanınızda " + symbol + " bulunamadı!"));

        if (asset.getQuantity().compareTo(amount) < 0) {
            throw new TradeException("Yetersiz miktar! Satmak istediğiniz: " + amount + ", Elinizdeki: " + asset.getQuantity());
        }

        // 2. Coin miktarını düş
        asset.setQuantity(asset.getQuantity().subtract(amount));
        assetRepository.save(asset);

        // 3. Nakit bakiyeyi artır (Satıştan gelen para)
        User user = userRepository.findById(userId).orElseThrow();
        BigDecimal totalRevenue = currentPrice.multiply(amount);
        user.setBalance(user.getBalance().add(totalRevenue));
        userRepository.save(user);

        saveOrder(userId, symbol, amount, currentPrice, "SELL");
        // İşlem Geçmişini (Makbuz) Kaydet
        TradeHistory history = new TradeHistory();
        history.setUserId(userId);
        history.setSymbol(symbol);
        history.setSide("SELL");
        history.setQuantity(amount);
        history.setPrice(currentPrice);
        history.setTotalAmount(totalRevenue);
        history.setTimestamp(java.time.LocalDateTime.now());
        tradeHistoryRepository.save(history);
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

    public PortfolioResponse getPortfolio(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TradeException("Kullanıcı bulunamadı! ID: " + userId));

        List<Asset> assets = assetRepository.findAllByUserId(userId);

        BigDecimal totalAssetsValue = BigDecimal.ZERO;
        BigDecimal totalUnrealizedPnl = BigDecimal.ZERO;
        List<PortfolioResponse.AssetDetail> assetDetails = new ArrayList<>();

        for (Asset asset : assets) {
            BigDecimal currentPrice = priceService.getPrice(asset.getSymbol());
            BigDecimal assetValue = asset.getQuantity().multiply(currentPrice);
            totalAssetsValue = totalAssetsValue.add(assetValue);

            // P&L Hesabı
            BigDecimal avgCost = asset.getAverageCost() != null ? asset.getAverageCost() : BigDecimal.ZERO;
            BigDecimal costBasis = avgCost.multiply(asset.getQuantity()); // Toplam alış maliyeti
            BigDecimal unrealizedPnl = assetValue.subtract(costBasis);   // Kar/Zarar ($)

            // Yüzde hesabı — sıfıra bölme koruması
            BigDecimal unrealizedPnlPercent = BigDecimal.ZERO;
            if (costBasis.compareTo(BigDecimal.ZERO) > 0) {
                unrealizedPnlPercent = unrealizedPnl
                        .divide(costBasis, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            totalUnrealizedPnl = totalUnrealizedPnl.add(unrealizedPnl);

            PortfolioResponse.AssetDetail detail = new PortfolioResponse.AssetDetail();
            detail.setSymbol(asset.getSymbol());
            detail.setQuantity(asset.getQuantity());
            detail.setAverageCost(avgCost);
            detail.setCurrentPrice(currentPrice);
            detail.setTotalValue(assetValue);
            detail.setUnrealizedPnl(unrealizedPnl);
            detail.setUnrealizedPnlPercent(unrealizedPnlPercent);
            assetDetails.add(detail);
        }

        // Toplam portföy P&L yüzdesi
        BigDecimal totalCostBasis = assets.stream()
                .map(a -> (a.getAverageCost() != null ? a.getAverageCost() : BigDecimal.ZERO)
                        .multiply(a.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPnlPercent = BigDecimal.ZERO;
        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            totalPnlPercent = totalUnrealizedPnl
                    .divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        PortfolioResponse response = new PortfolioResponse();
        response.setUserId(userId);
        response.setCashBalance(user.getBalance());
        response.setTotalAssetsValue(totalAssetsValue);
        response.setTotalPortfolioValue(user.getBalance().add(totalAssetsValue));
        response.setTotalUnrealizedPnl(totalUnrealizedPnl);
        response.setTotalUnrealizedPnlPercent(totalPnlPercent);
        response.setAssets(assetDetails);

        return response;
    }
}