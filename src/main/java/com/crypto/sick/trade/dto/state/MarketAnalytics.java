package com.crypto.sick.trade.dto.state;

import com.bybit.api.client.domain.market.MarketInterval;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Builder
public class MarketAnalytics {

    @Builder.Default
    private Map<MarketInterval, MarketPercentile> marketPercentiles = new HashMap<>();

    public MarketPercentile getMarketPercentile(MarketInterval interval) {
        return Optional.ofNullable(marketPercentiles.get(interval))
                .orElseGet(() -> updateMarketPercentile(interval, MarketPercentile.empty()));
    }

    public MarketPercentile updateMarketPercentile(MarketInterval marketInterval, MarketPercentile marketPercentile) {
        return marketPercentiles.put(marketInterval, marketPercentile);
    }

    public static MarketAnalytics empty() {
        return MarketAnalytics.builder().build();
    }

}
