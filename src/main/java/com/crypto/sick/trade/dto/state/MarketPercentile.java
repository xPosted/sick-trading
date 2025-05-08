package com.crypto.sick.trade.dto.state;

import com.crypto.sick.trade.dto.enums.PercentileEnum;
import lombok.Builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Builder
public class MarketPercentile {

    @Builder.Default
    private Map<PercentileEnum, PercentileValues> percentiles = new HashMap<>();

    public void updatePercentile(PercentileEnum percentile, PercentileValues values) {
        percentiles.put(percentile, values);
    }

    public Optional<PercentileValues> getPercentile(PercentileEnum percentile) {
        return Optional.ofNullable(percentiles.get(percentile));
    }

    public static MarketPercentile empty() {
        return MarketPercentile.builder().build();
    }

}
