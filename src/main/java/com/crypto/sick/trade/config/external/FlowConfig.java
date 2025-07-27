package com.crypto.sick.trade.config.external;

import com.crypto.sick.trade.dto.enums.StrategyEnum;
import lombok.Data;

import java.util.Map;

@Data
public class FlowConfig {

    Map<StrategyEnum, StrategyConfig> strategies;
    boolean syncStrategies;
    Double stopLoss;
    Double takeProfit;

}
