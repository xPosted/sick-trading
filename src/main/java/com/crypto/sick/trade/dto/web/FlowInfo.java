package com.crypto.sick.trade.dto.web;

import com.crypto.sick.trade.data.user.FlowState;
import com.crypto.sick.trade.data.user.StrategyState;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class FlowInfo {

    FlowTypeEnum flowType;
    @With
    TradingStrategyStatusEnum status;
    Map<StrategyEnum, StrategyInfo> strategies;
    boolean syncStrategies;

    public static FlowInfo from(FlowState flowState) {
        var strategies = flowState.getStrategies().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> StrategyInfo.fromStrategyState(entry.getValue())));
        return FlowInfo.builder()
                .flowType(flowState.getFlowType())
                .status(flowState.getStatus())
                .syncStrategies(flowState.isSyncStrategies())
                .strategies(strategies)
                .build();
    }

}
