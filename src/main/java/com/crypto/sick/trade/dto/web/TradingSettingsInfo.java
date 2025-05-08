package com.crypto.sick.trade.dto.web;

import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradingSettingsInfo {

    double buyAmount;
    double sellAmount;
    double tradeOffset;
    double stopLoss;
    int stopLossTimeout;
    List<StrategyInfo> strategies;

    public static TradingSettingsInfo map(CoinIntervalTradingState coinIntervalTradingState) {
        return TradingSettingsInfo.builder()
                .buyAmount(coinIntervalTradingState.getBuyAmount())
                .sellAmount(coinIntervalTradingState.getSellAmount())
                .stopLoss(coinIntervalTradingState.getStopLoss())
                .stopLossTimeout(coinIntervalTradingState.getStopLossTimeout())
                .build();

    }

}
