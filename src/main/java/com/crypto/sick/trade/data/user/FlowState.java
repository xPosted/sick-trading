package com.crypto.sick.trade.data.user;

import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.service.strategy.StrategyEvaluationResult;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum.SLEEPING;
import static java.util.Optional.ofNullable;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FlowState {

    FlowTypeEnum flowType;
    @With
    TradingStrategyStatusEnum status;
    Map<StrategyEnum, StrategyState> strategies;
    boolean syncStrategies;

    public FlowState updateStatus(StrategyEvaluationResult strategyEvaluationResult) {
        var targetStrategyState = strategies.get(strategyEvaluationResult.getStrategy());
        var newStatus = ofNullable(strategyEvaluationResult.getTradingStatus()).orElse(targetStrategyState.getStatus());
        var newTimestamp = ofNullable(strategyEvaluationResult.getTimestamp()).orElse(targetStrategyState.getStatusTime());
        var newPrice = ofNullable(strategyEvaluationResult.getLastPrice()).orElse(targetStrategyState.getLastPrice());
        var updatedStrategyState = targetStrategyState.toBuilder()
                .status(newStatus)
                .statusTime(newTimestamp)
                .lastPrice(newPrice)
                .build();
        var updatedStrategyStates = new HashMap<>(strategies);
        updatedStrategyStates.put(strategyEvaluationResult.getStrategy(), updatedStrategyState);
        return this.toBuilder()
                .status(getTopStatus(updatedStrategyStates.values()))
                .strategies(updatedStrategyStates)
                .build();
    }

    public FlowState forceStatus(TradingStrategyStatusEnum status) {
        var updatedStrategies = strategies.values().stream()
                .map(strategyState -> strategyState.withStatus(status))
                .collect(Collectors.toMap(StrategyState::getStrategy, strategyState -> strategyState));
        return this.toBuilder()
                .status(status)
                .strategies(updatedStrategies)
                .build();
    }

    public TradingStrategyStatusEnum getTopStatus(Collection<StrategyState> strategyStates) {
        var minPriorityStrategyStatus = getMinPriorityStrategyStatus(strategyStates);
        var maxPriorityStrategyStatus = getMaxPriorityStrategyStatus(strategyStates);
        if (isSyncStrategies()) {
            if (minPriorityStrategyStatus.getPriority() <= SLEEPING.getPriority()) {
               return minPriorityStrategyStatus;
            }
        }
        return maxPriorityStrategyStatus;
    }

    private TradingStrategyStatusEnum getMinPriorityStrategyStatus(Collection<StrategyState> strategyStates) {
        return strategyStates.stream()
                .map(StrategyState::getStatus)
                .min(Comparator.comparingInt(TradingStrategyStatusEnum::getPriority))
                .orElseThrow();
    }

    private TradingStrategyStatusEnum getMaxPriorityStrategyStatus(Collection<StrategyState> strategyStates) {
        return strategyStates.stream()
                .map(StrategyState::getStatus)
                .max(Comparator.comparingInt(TradingStrategyStatusEnum::getPriority))
                .orElseThrow();
    }
}
