package com.crypto.sick.trade.config.external;

import com.crypto.sick.trade.dto.enums.StrategyEnum;
import lombok.Data;

@Data
public class StrategyConfig {

    StrategyEnum type;
    double priceOffset;
    int stopLossTimeout;
    double lowCriticalValue;
    double highCriticalValue;
    double indicatorOffset;
    double hedgeTp;
    double hedgeSl;

}
