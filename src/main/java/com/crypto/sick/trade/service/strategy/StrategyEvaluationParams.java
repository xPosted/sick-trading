package com.crypto.sick.trade.service.strategy;

import com.crypto.sick.trade.data.user.OrderContext;
import com.crypto.sick.trade.data.user.StrategyState;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TaapiIntervalEnum;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.function.Function;

@Value
@Builder
public class StrategyEvaluationParams {

    FlowTypeEnum flowType;
    StrategyState strategyState;
    Symbol symbol;
    TaapiIntervalEnum interval;
    Function<Double, Boolean> isAvailableToSell;
    Function<Double, Boolean> isAvailableToBuy;
    Double takeProfit;
    Double stopLoss;
    List<OrderContext> lastOrders;

}
