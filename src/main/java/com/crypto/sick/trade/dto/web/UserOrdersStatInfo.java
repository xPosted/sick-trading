package com.crypto.sick.trade.dto.web;

import com.crypto.sick.trade.data.user.CategoryTradingState;
import com.crypto.sick.trade.dto.enums.OrderOperationTypeEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CoinTradingState;
import com.crypto.sick.trade.data.user.UserStateEntity;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.stream.Collectors;

import static com.crypto.sick.trade.dto.enums.OrderOperationTypeEnum.BUY_OPERATION;

@Data
@Builder
public class UserOrdersStatInfo {

    OrdersStatInfo total;
    Map<Symbol, OrdersStatInfo> symbolOrdersStat;



    public static UserOrdersStatInfo build(CategoryTradingState categoryTradingState) {
        var symbolOrdersStat = categoryTradingState.getCoinTradingStates().values().stream()
                .collect(Collectors.toMap(CoinTradingState::getSymbol, UserOrdersStatInfo::buildSymbolOrdersStat));
        var totalBuy = symbolOrdersStat.values().stream()
                .mapToInt(OrdersStatInfo::getTotalBuy)
                .sum();
        var totalSell = symbolOrdersStat.values().stream()
                .mapToInt(OrdersStatInfo::getTotalSell)
                .sum();
        var totalStopLoss = symbolOrdersStat.values().stream()
                .mapToInt(OrdersStatInfo::getTotalStopLoss)
                .sum();

        return UserOrdersStatInfo.builder()
            .total(OrdersStatInfo.builder()
                .totalBuy(totalBuy)
                .totalSell(totalSell)
                .totalStopLoss(totalStopLoss)
                .build())
            .symbolOrdersStat(symbolOrdersStat)
            .build();
    }

    public static OrdersStatInfo buildSymbolOrdersStat(CoinTradingState coinTradingState) {
        var symbolOrders =  coinTradingState.getIntervalStates().values().stream()
                .map(CoinIntervalTradingState::getOrderHistory)
                .flatMap(orderHistory -> orderHistory.stream())
                .collect(Collectors.toSet());
        var buyCount = (int) symbolOrders.stream().filter(order -> order.getOperationType().equals(BUY_OPERATION)).count();
        var sellCount = (int) symbolOrders.stream().filter(order -> order.getOperationType().equals(OrderOperationTypeEnum.SELL_OPERATION)).count();
        var stopLossCount = (int) symbolOrders.stream().filter(order -> order.getOperationType().equals(OrderOperationTypeEnum.STOP_LOSS_OPERATION)).count();

        return OrdersStatInfo.builder()
            .totalBuy(buyCount)
            .totalSell(sellCount)
            .totalStopLoss(stopLossCount)
            .build();
    }

@Data
@Builder
public static final class OrdersStatInfo {
    int totalBuy;
    int totalSell;
    int totalStopLoss;
}

}
