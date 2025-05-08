package com.crypto.sick.trade.dto.web.taapi;


import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TaapiIndicatorEnum;
import com.crypto.sick.trade.dto.enums.TaapiIntervalEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
@Builder
public class TaapiBulkResult {

    @Builder.Default
    Map<Symbol, TaapiIndicatorResult> results = new HashMap<>();

    public TaapiIndicatorResult getOrCreate(Symbol symbol) {
        if (!results.containsKey(symbol)) {
            results.put(symbol, TaapiIndicatorResult.builder().symbol(symbol).build());
        }
        return results.get(symbol);
    }

    public void addIndicatorResult(IndicatorResponse indicatorResponse) {
        var objectMapper = new ObjectMapper();
        var splitedId = indicatorResponse.getId().split(":");
        var symbol = objectMapper.convertValue(splitedId[0], Symbol.class);
        var interval = objectMapper.convertValue(splitedId[1], TaapiIntervalEnum.class);
        var indicator = objectMapper.convertValue(splitedId[2], TaapiIndicatorEnum.class);
        getOrCreate(symbol).add(indicator, interval, Double.parseDouble(indicatorResponse.getValue()));
    }

}
