package com.crypto.sick.trade.dto.web;

import com.crypto.sick.trade.data.user.StrategyState;
import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import lombok.Builder;
import lombok.Data;
import lombok.With;

@Builder
@Data
public class StrategyInfo {

    StrategyEnum strategy;
    @With
    TradingStrategyStatusEnum status;
    Long statusTime;
    double tradeOffset;
    double lowCriticalValue;
    double highCriticalValue;
    Double lastPrice;


    public static StrategyInfo fromStrategyState(StrategyState state) {
        return StrategyInfo.builder()
                .strategy(state.getStrategy())
                .status(state.getStatus())
                .statusTime(state.getStatusTime())
                .lowCriticalValue(state.getLowCriticalValue())
                .highCriticalValue(state.getHighCriticalValue())
                .lastPrice(state.getLastPrice())
                .tradeOffset(state.getPriceOffset())
                .build();
    }

    public StrategyState toStrategyState() {
        return StrategyState.builder()
                .strategy(strategy)
                .status(status)
                .statusTime(statusTime)
                .lowCriticalValue(lowCriticalValue)
                .highCriticalValue(highCriticalValue)
                .lastPrice(lastPrice)
                .priceOffset(tradeOffset)
                .build();
    }
}
