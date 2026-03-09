package com.yigitusq.orderservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PortfolioResponse {
    private Long userId;
    private BigDecimal cashBalance;        // Nakit Doları
    private BigDecimal totalAssetsValue;   // Coinlerinin toplam dolar değeri
    private BigDecimal totalPortfolioValue;// Nakit + Coinlerin toplamı
    private List<AssetDetail> assets;      // Hangi coinden ne kadar var?

    @Data
    public static class AssetDetail {
        private String symbol;
        private BigDecimal quantity;
        private BigDecimal currentPrice;
        private BigDecimal totalValue;
    }
}