package com.crypto.sick.trade.dto.web;

import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TaapiIntervalEnum;
import com.crypto.sick.trade.data.user.CoinTradingState;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class CoinTradingInfo {

    Symbol symbol;
    Map<TaapiIntervalEnum, CoinIntervalTradingInfo> IntervalInfos;

    public static CoinTradingInfo map(CoinTradingState coinTradingState, Double lastPrice) {
        var intervalInfos = coinTradingState.getIntervalStates().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> CoinIntervalTradingInfo.map(entry.getValue(), lastPrice)));
        return CoinTradingInfo.builder()
                .symbol(coinTradingState.getSymbol())
                .IntervalInfos(intervalInfos)
                .build();
    }

}
