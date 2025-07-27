package com.crypto.sick.trade.service.strategy;

import com.crypto.sick.trade.dto.enums.StrategyEnum;

public interface TradingStrategy {
    StrategyEnum getStrategyName();
    StrategyEvaluationResult evaluate(StrategyEvaluationParams params);

    default boolean validStrategy(StrategyEvaluationParams params) {
        return getStrategyName().equals(params.getStrategyState().getStrategy());
    }
}
