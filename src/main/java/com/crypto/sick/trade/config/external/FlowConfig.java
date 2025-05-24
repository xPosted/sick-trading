package com.crypto.sick.trade.config.external;

import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FlowConfig {

    Map<StrategyEnum, StrategyConfig> strategies;
    boolean syncStrategies;
    Double stopLoss;
    Double takeProfit;

}
