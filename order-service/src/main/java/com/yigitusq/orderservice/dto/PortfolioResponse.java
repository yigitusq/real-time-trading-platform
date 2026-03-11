package com.yigitusq.orderservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PortfolioResponse {
    private Long userId;
    private BigDecimal cashBalance;
    private BigDecimal totalAssetsValue;
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalUnrealizedPnl;        // toplam kar/zarar ($)
    private BigDecimal totalUnrealizedPnlPercent; // toplam kar/zarar (%)
    private List<AssetDetail> assets;

    @Data
    public static class AssetDetail {
        private String symbol;
        private BigDecimal quantity;
        private BigDecimal averageCost;
        private BigDecimal currentPrice;
        private BigDecimal totalValue;
        private BigDecimal unrealizedPnl;        // bu coinin kar/zarar ($)
        private BigDecimal unrealizedPnlPercent; // bu coinin kar/zarar (%)
    }
}