package com.crypto.sick.trade.service.strategy;

import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.FlowState;
import com.crypto.sick.trade.data.user.StrategyState;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.StrategyEnum;

import java.util.List;

public class StrategyEvaluationParamsBuilder {

    public static List<StrategyEvaluationParams> buildStrategyEvaluationParams(FlowTypeEnum flowType,
                                                                         CoinIntervalTradingState coinIntervalTradingState) {
        var flowState = coinIntervalTradingState.getFlowStates().get(flowType);
        return flowState.getStrategies().keySet().stream()
                .map(strategy -> buildStrategyEvaluationParams(flowType, strategy, coinIntervalTradingState))
                .toList();
    }

    private static StrategyEvaluationParams buildStrategyEvaluationParams(FlowTypeEnum flowType, StrategyEnum strategy, CoinIntervalTradingState coinIntervalTradingState) {
        var flowState = coinIntervalTradingState.getFlowStates().get(flowType);
        var strategyState = flowState.getStrategies().get(strategy);
        return switch (flowType) {
            case MAIN_FLOW -> StrategyEvaluationParams.builder()
                    .flowType(flowType)
                    .isAvailableToSell(coinIntervalTradingState::isAvailableToSell)
                    .isAvailableToBuy(coinIntervalTradingState::isAvailableToBuy)
                    .interval(coinIntervalTradingState.getInterval())
                    .strategyState(strategyState)
                    .symbol(coinIntervalTradingState.getSymbol())
                    .build();
            case TAKE_PROFIT_FLOW -> StrategyEvaluationParams.builder()
                    .flowType(flowType)
                    .isAvailableToSell(lastPrice -> coinIntervalTradingState.isAvailableToCloseLongPosition())
                    .isAvailableToBuy(lastPrice -> coinIntervalTradingState.isAvailableToCloseShortPosition())
                    .interval(coinIntervalTradingState.getInterval())
                    .strategyState(strategyState)
                    .symbol(coinIntervalTradingState.getSymbol())
                    .build();
        };
    }

}
