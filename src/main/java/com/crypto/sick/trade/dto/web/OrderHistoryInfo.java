package com.crypto.sick.trade.dto.web;

import com.crypto.sick.trade.data.user.CoinTradingState;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TaapiIntervalEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.stream.Collectors;

import static com.crypto.sick.trade.dto.web.OrderIntervalHistoryInfo.buildOrderIntervalHistoryInfo;

@Data
@Builder
public class OrderHistoryInfo {

    Symbol symbol;
    Map<TaapiIntervalEnum, OrderIntervalHistoryInfo> intervalsOrderHistory;

    public static OrderHistoryInfo buildOrderHistoryInfo(CoinTradingState coinTradingState) {
        var intervalHistory = coinTradingState.getIntervalStates().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> buildOrderIntervalHistoryInfo(entry.getValue())));
        return OrderHistoryInfo.builder()
                .symbol(coinTradingState.getSymbol())
                .intervalsOrderHistory(intervalHistory)
                .build();

    }

}
