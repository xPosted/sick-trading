package com.crypto.sick.trade.dto.web.taapi;

import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TaapiIndicatorEnum;
import com.crypto.sick.trade.dto.enums.TaapiIntervalEnum;
import lombok.Builder;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
@Builder
public class TaapiIndicatorResult {

    Symbol symbol;
    @Builder.Default
    Map<TaapiIntervalEnum, Double> rsiValues = new HashMap<>();
    @Builder.Default
    Map<TaapiIntervalEnum, Double> mfiValues = new HashMap<>();;

    public void add(TaapiIndicatorEnum indicator, TaapiIntervalEnum interval, double value) {
        switch (indicator) {
            case RSI -> rsiValues.put(interval, value);
            case MFI -> mfiValues.put(interval, value);
            default -> throw new IllegalArgumentException("Unsupported indicator: " + indicator);
        }
    }

}
