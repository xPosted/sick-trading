package com.crypto.sick.trade.dto.state;

import com.bybit.api.client.domain.market.MarketInterval;
import com.crypto.sick.trade.dto.MarketLinesInfo;
import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TaapiIntervalEnum;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
@Builder
public class MarketState {

    Symbol symbol;
    @Builder.Default
    Map<MarketInterval, MarketLinesInfo> marketLinesInfoMap = new HashMap<>();
    Double lastPrice;
    @Builder.Default
    Map<TaapiIntervalEnum, Double> rsi = new HashMap<>();
    @Builder.Default
    Map<TaapiIntervalEnum, Double> mfi = new HashMap<>();
    @Builder.Default
    @Getter
    MarketAnalytics marketAnalytics = MarketAnalytics.empty();

    public void updateRsi(TaapiIntervalEnum interval, Double rsi) {
        var rsiScaled = BigDecimal.valueOf(rsi).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
        this.rsi.put(interval, rsiScaled);
    }

    public void updateMfi(TaapiIntervalEnum interval, Double mfi) {
        var mfiScaled = BigDecimal.valueOf(mfi).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
        this.mfi.put(interval, mfiScaled);
    }

    public Double getIndicator(TaapiIntervalEnum interval, StrategyEnum strategy) {
        return switch (strategy) {
            case RSI_STRATEGY -> Optional.ofNullable(rsi.get(interval))
                    .orElse(-1D);
            case MFI_STRATEGY -> Optional.ofNullable(mfi.get(interval))
                    .orElse(-1D);
            default -> throw new RuntimeException("No any indicator found for strategy: " + strategy);
        };
    }

    public void update(MarketLinesInfo marketLinesInfo) {
        marketLinesInfoMap.put(marketLinesInfo.getMarketDataRequest().getMarketInterval(), marketLinesInfo);
    }

    public Optional<MarketLinesInfo> get(MarketInterval marketInterval) {
        return Optional.ofNullable(marketLinesInfoMap.get(marketInterval));
    }

    public static MarketState buildEmpty(Symbol symbol) {
        return MarketState.builder()
                .symbol(symbol)
                .build();
    }
}
