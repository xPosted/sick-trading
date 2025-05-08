package com.crypto.sick.trade.dto.web;

import com.bybit.api.client.domain.trade.Side;
import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.OrderContext;
import com.crypto.sick.trade.util.Utils;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparingLong;

@Data
@Builder
public class OrderIntervalHistoryInfo {

    Double avgBuyPrice;
    Double avgSellPrice;
    Double avgProfit;
    List<OrderContext> orderHistory;

    public static OrderIntervalHistoryInfo buildOrderIntervalHistoryInfo(CoinIntervalTradingState coinIntervalTradingState) {
        var avgBuy = coinIntervalTradingState.getOrderHistory().stream()
                .filter(orderContext -> orderContext.getOrderResultInfo().getSide().equals(Side.BUY))
                .map(OrderContext::getLastPrice)
                .mapToDouble(Double::doubleValue)
                .average().orElse(0.0);
        var avgSell = coinIntervalTradingState.getOrderHistory().stream()
                .filter(orderContext -> orderContext.getOrderResultInfo().getSide().equals(Side.SELL))
                .map(OrderContext::getLastPrice)
                .mapToDouble(Double::doubleValue)
                .average().orElse(0.0);
        var sortedOrders = coinIntervalTradingState.getOrderHistory().stream()
                .sorted(comparingLong(context -> context.getOrderResultInfo().getTime()))
                .toList();
        return OrderIntervalHistoryInfo.builder()
                .avgBuyPrice(avgBuy)
                .avgSellPrice(avgSell)
                .avgProfit(avgProfit(sortedOrders))
                .orderHistory(sortedOrders)
                .build();
    }

    private static Double avgProfit(List<OrderContext> sortedOrders) {
        return sortedOrders.stream()
                .filter(orderContext -> orderContext.getOrderResultInfo().getSide().equals(Side.BUY))
                .mapToDouble(orderContext -> {
                    var buyPrice = orderContext.getLastPrice();
                    var sellPrice = findSellPrice(orderContext.getOrderResultInfo().getTime(), sortedOrders)
                            .orElse(buyPrice);
                    return Utils.calculateDiffPrcnt(buyPrice, sellPrice);
                })
                .average().orElse(0.0);
    }

    private static Optional<Double> findSellPrice(Long buyTime, List<OrderContext> sortedOrders) {
        return sortedOrders.stream()
                .filter(orderContext -> orderContext.getOrderResultInfo().getSide().equals(Side.SELL))
                .filter(orderContext -> orderContext.getOrderResultInfo().getTime() > buyTime)
                .map(OrderContext::getLastPrice)
                .findFirst();
    }

}
