package com.crypto.sick.trade.service;

import com.crypto.sick.trade.data.user.*;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.web.bybit.ws.orders.WsOrderResponseDto;
import com.crypto.sick.trade.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

@Service
@Slf4j
public class StopLossService {

    @Autowired
    private UserService userService;
    @Autowired
    private TradeOperationService tradeOperationService;

    public void onCloseOrderEvent(String userName, WsOrderResponseDto.OrderData orderData) {
        var userState = userService.findByName(userName)
                .orElseThrow(() -> new RuntimeException("User not found: " + userName));
        var targetCategory = userState.getCategoryTradingStates().get(orderData.getCategory());
        var targetCoin = targetCategory.getCoinTradingStates().get(orderData.getSymbol());
        var updatedCoinState = onCloseOrderEvent(targetCoin, orderData);
        var updatedCategory = targetCategory.withUpdatedCoinState(orderData.getSymbol(), updatedCoinState);
        var updatedUserState = userState.withUpdatedCategory(updatedCategory);
        userService.save(evaluateOnStopLossCondition(updatedUserState));

    }

    private CoinTradingState onCloseOrderEvent(CoinTradingState coinTradingState, WsOrderResponseDto.OrderData orderData) {
        CoinIntervalTradingState targetIntervalState = coinTradingState.getIntervalStates().values().stream()
                .filter(this::hasActivePositionPredicate)
                .findFirst().orElseThrow(() -> new RuntimeException("No active position found for " + coinTradingState.getSymbol()));
        var updatesIntervalState = targetIntervalState.closeLastSuccessfulPosition(Utils.inverse(orderData.getSide()), FlowTypeEnum.MAIN_FLOW, orderData.getStopOrderType());
        return coinTradingState.withUpdatedIntervalState(updatesIntervalState.getInterval(), updatesIntervalState);

//            var orderContext = targetIntervalState.getLastSuccessfulOrderHistoryItem(FlowTypeEnum.MAIN_FLOW)
//                    .orElseThrow(() -> new RuntimeException("No last successful order found for " + coinTradingState.getSymbol()));
//            var updatedOrder = orderContext.withClosed(true)
//                    .withStopOrderType(orderData.getStopOrderType());
//            var updatedOrdersHistory = targetIntervalState.getOrderHistory().

    }

    private boolean hasActivePositionPredicate(CoinIntervalTradingState coinIntervalTradingState) {
        return coinIntervalTradingState.getLastSuccessfulOrderHistoryItem(FlowTypeEnum.MAIN_FLOW)
                .filter(orderContext -> !orderContext.isClosed())
                .isPresent();
    }

    public UserStateEntity evaluateOnStopLossCondition(UserStateEntity userState) {
        if (stopLossConditionMet(userState)) {
            var stopLossExpirationTs = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();
            log.info("Stop loss condition met for user: {}, setting status to STOP_LOSS with expiration at {}", userState.getName(), stopLossExpirationTs);
            tradeOperationService.closeAllOpenPositions(userState);
            return userState.withStatus(UserStatusEntity.of(UserStatusEnum.STOP_LOSS, stopLossExpirationTs));
        }
        return userState;
    }

    private boolean stopLossConditionMet(UserStateEntity userState) {
        var sortedOrders = userState.getCategoryTradingStates().values().stream()
                .flatMap(categoryTradingState -> categoryTradingState.getCoinTradingStates().values().stream())
                .flatMap(coinTradingState -> coinTradingState.getIntervalStates().values().stream())
                .flatMap(intervalState -> intervalState.getLastSuccessfulOrderHistoryItem(FlowTypeEnum.MAIN_FLOW).stream())
                .filter(OrderContext::isClosed)
                .filter(orderContext -> ! orderContext.isOlderThanLastHour())
                .sorted(Comparator.comparingLong(OrderContext::getStopOrderTs)).toList();

        var lastThreeOrders = sortedOrders.size() >= 2
                ? sortedOrders.subList(sortedOrders.size() - 2, sortedOrders.size())
                : sortedOrders;
        if (lastThreeOrders.size() < 2) {
            return false; // closed orders to evaluate not enough
        }
        return lastThreeOrders.stream()
                .allMatch(OrderContext::isClosedByStopLoss);
    }

}
