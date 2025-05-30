package com.crypto.sick.trade.service.strategy;

import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.FlowState;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.StrategyEnum;

import java.util.List;

public class StrategyEvaluationParamsBuilder {

    public static List<StrategyEvaluationParams> buildStrategyEvaluationParams(FlowTypeEnum flowType,
                                                                               CoinIntervalTradingState coinIntervalTradingState) {
        var flowState = coinIntervalTradingState.getFlowStates().get(flowType);
        return flowState.getStrategies().keySet().stream()
                .map(strategy -> buildStrategyEvaluationParams(flowState, strategy, coinIntervalTradingState))
                .toList();
    }

    private static StrategyEvaluationParams buildStrategyEvaluationParams(FlowState flowState, StrategyEnum strategy, CoinIntervalTradingState coinIntervalTradingState) {
        var strategyState = flowState.getStrategies().get(strategy);
        var flowType = flowState.getFlowType();
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
            case HEDGE_FLOW -> StrategyEvaluationParams.builder()
                    .flowType(flowType)
                    .interval(coinIntervalTradingState.getInterval())
                    .strategyState(strategyState)
                    .symbol(coinIntervalTradingState.getSymbol())
                    .lastOrder(coinIntervalTradingState.getLastSuccessfulOrderHistoryItem(FlowTypeEnum.MAIN_FLOW))
                    .takeProfit(flowState.getTakeProfit())
                    .stopLoss(flowState.getStopLoss())
                    .build();
            case OVERTAKE_FLOW -> StrategyEvaluationParams.builder()
                    .flowType(flowType)
                    .interval(coinIntervalTradingState.getInterval())
                    .strategyState(strategyState)
                    .symbol(coinIntervalTradingState.getSymbol())
                    .lastOrder(coinIntervalTradingState.getLastSuccessfulOrderHistoryItem(FlowTypeEnum.MAIN_FLOW))
                    .isAvailableToSell(coinIntervalTradingState::isAvailableToOverTake)
                    .isAvailableToBuy(coinIntervalTradingState::isAvailableToOverTake)
                    .takeProfit(flowState.getTakeProfit())
                    .stopLoss(flowState.getStopLoss())
                    .build();
            case CHAIN_FLOW -> StrategyEvaluationParams.builder()
                    .flowType(flowType)
                    .interval(coinIntervalTradingState.getInterval())
                    .strategyState(strategyState)
                    .symbol(coinIntervalTradingState.getSymbol())
                    .takeProfit(flowState.getTakeProfit())
                    .stopLoss(flowState.getStopLoss())
                    .build();
        };
    }

}
