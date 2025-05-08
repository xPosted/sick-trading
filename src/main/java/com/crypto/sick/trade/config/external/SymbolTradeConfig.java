package com.crypto.sick.trade.config.external;

import com.crypto.sick.trade.dto.enums.TaapiIntervalEnum;
import lombok.Data;

import java.util.Map;

@Data
public class SymbolTradeConfig {

    Map<TaapiIntervalEnum, SymbolIntervalTradeConfig> intervals;

}
