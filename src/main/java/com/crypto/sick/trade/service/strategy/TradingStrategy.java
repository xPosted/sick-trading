package com.crypto.sick.trade.service.strategy;

import com.crypto.sick.trade.data.user.CoinIntervalTradingState;

public interface TradingStrategy {
    StrategyEvaluationResult evaluate(StrategyEvaluationParams params);
}
