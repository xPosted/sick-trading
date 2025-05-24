package com.crypto.sick.trade.config.external;

import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import lombok.Data;

import java.util.Map;

@Data
public class SymbolIntervalTradeConfig {

    double buyAmount;
    double sellAmount;
    int leverage;
    boolean syncStrategies;
    Map<FlowTypeEnum, FlowConfig> flows;

}
