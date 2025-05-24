package com.crypto.sick.trade.data.user;

import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategyState {

    StrategyEnum strategy;
    @With
    TradingStrategyStatusEnum status;
    Long statusTime;
    int stopLossTimeout;
    Double lowCriticalValue;
    Double highCriticalValue;
    double priceOffset;
    double indicatorOffset;
    Double lastPrice;

}
