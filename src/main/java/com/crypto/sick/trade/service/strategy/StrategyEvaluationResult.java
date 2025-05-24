package com.crypto.sick.trade.service.strategy;

import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Builder(toBuilder = true)
@Value
public class StrategyEvaluationResult {

    StrategyEnum strategy;
    @With
    TradingStrategyStatusEnum tradingStatus;
    Double lastPrice;
    Double highCriticalValue;
    Double lowCriticalValue;
    Long timestamp;

}
