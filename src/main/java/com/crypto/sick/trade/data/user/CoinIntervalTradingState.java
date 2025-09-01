package com.crypto.sick.trade.data.user;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.StopOrderType;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TaapiIntervalEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.bybit.api.client.domain.trade.Side.BUY;
import static com.bybit.api.client.domain.trade.Side.SELL;
import static com.crypto.sick.trade.dto.enums.FlowTypeEnum.MAIN_FLOW;
import static com.crypto.sick.trade.dto.enums.FlowTypeEnum.OVERTAKE_FLOW;
import static com.crypto.sick.trade.util.Utils.FIVE_POINT_THREE;
import static com.crypto.sick.trade.util.Utils.ZERO;
import static java.util.Objects.nonNull;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinIntervalTradingState {

    CategoryType category;
    Symbol symbol;
    TaapiIntervalEnum interval;
    Map<FlowTypeEnum, FlowState> flowStates;
    double acquiredQty;
    double buyAmount;
    double sellAmount;
    int leverage;
    int stopLossTimeout;
    @Builder.Default
    Set<OrderContext> orderHistory = new HashSet<>();

    public CoinIntervalTradingState withUpdatedFlowState(FlowState updatedFlowState) {
        var updatedFlowStates = new HashMap<>(flowStates);
        updatedFlowStates.put(updatedFlowState.getFlowType(), updatedFlowState);
        return this.toBuilder()
                .flowStates(updatedFlowStates)
                .build();
    }

    public CoinIntervalTradingState withOrder(OrderContext orderContext) {
        var updatedHistory = new HashSet<>(orderHistory);
        updatedHistory.add(orderContext);
        return this.toBuilder()
                .orderHistory(updatedHistory)
                .acquiredQty(acquiredQty + orderContext.getAcquiredQty())
                .build();
    }

    @JsonIgnore
    public CoinIntervalTradingState disable() {
        var updatedFlowStates = flowStates.values().stream()
                .map(flow -> flow.forceStatus(TradingStrategyStatusEnum.DISABLED))
                .collect(Collectors.toMap(FlowState::getFlowType, flow -> flow));
        return this.toBuilder()
                .flowStates(updatedFlowStates)
                .build();
    }

    public CoinIntervalTradingState closeLastSuccessfulPosition(Side side, FlowTypeEnum flowType, StopOrderType stopOrderType) {
        var lastSuccessfulShortPosOptional = getLastSuccessfulOrderHistoryItem(flowType)
                .filter(o -> o.getSide().equals(side));
        if (lastSuccessfulShortPosOptional.isEmpty()) {
            log.warn("Can't find last successful position for side {}", side);
            return this;
        }
        var lastSuccessfulShortPos = lastSuccessfulShortPosOptional.get();
        var updatedOrders = orderHistory.stream()
                .filter(o -> nonNull(o.getOrderId()))
                .filter(o -> !o.getOrderId().equals(lastSuccessfulShortPos.getOrderId()))
                .collect(Collectors.toSet());
        updatedOrders.add(
                lastSuccessfulShortPos
                        .withClosed(true)
                        .withStopOrderType(stopOrderType)
                        .withStopOrderTs(Instant.now().toEpochMilli()));
        return toBuilder()
                .orderHistory(updatedOrders)
                .build();
    }

    public CoinIntervalTradingState updateStatus(FlowTypeEnum flow, TradingStrategyStatusEnum tradingStrategyStatus) {
        var updatedFlowState = flowStates.get(flow).withStatus(tradingStrategyStatus);
        var updatedFlowStates = new HashMap<>(flowStates);
        updatedFlowStates.put(flow, updatedFlowState);
        return this.toBuilder()
                .flowStates(updatedFlowStates)
                .build();
    }

    public CoinIntervalTradingState forceStatus(FlowTypeEnum flow, TradingStrategyStatusEnum tradingStrategyStatus) {
        var updatedFlowState = flowStates.get(flow).forceStatus(tradingStrategyStatus);
        var updatedFlowStates = new HashMap<>(flowStates);
        updatedFlowStates.put(flow, updatedFlowState);
        return this.toBuilder()
                .flowStates(updatedFlowStates)
                .build();
    }

    public CoinIntervalTradingState forceStatus(FlowTypeEnum flowTypeEnum, TradingStrategyStatusEnum status, OrderContext orderContext) {
        return forceStatus(flowTypeEnum, status)
                .withOrder(orderContext);
    }

    @JsonIgnore
    public Optional<OrderContext> getLastOrderHistoryItem() {
        return getOrderHistory().stream()
                .max(Comparator.comparingLong(context -> context.getOrderResultInfo().getTime()));
    }

    @JsonIgnore
    public Optional<OrderContext> getLastSuccessfulOrderHistoryItem(FlowTypeEnum flowType) {
        return getOrderHistory().stream()
                .filter(order -> order.getOrderResultInfo().getRetCode() == ZERO)
                .filter(order -> order.getFlowType().equals(flowType))// order should be successful
                .max(Comparator.comparingLong(context -> context.getOrderResultInfo().getTime()));
    }

    @JsonIgnore
    public boolean isAvailableToBuy(Double lastPrice) {
        var lastOrder = getLastSuccessfulOrderHistoryItem(MAIN_FLOW);
        switch (category) {
            case SPOT:
                var acquiredInUsdt = getAcquiredQty() * lastPrice;
                if (acquiredInUsdt < FIVE_POINT_THREE) {
                    return true;
                }
                return lastOrder.isEmpty() ||
                        lastOrder
                                .filter(orderContext -> orderContext.getSide().equals(SELL))
                                .isPresent();

            case LINEAR: {
                return lastOrder.isEmpty() ||
                        lastOrder.filter(OrderContext::isShortOperationType).isPresent() ||
                        lastOrder.filter(OrderContext::isOlderThanLast12Hours).isPresent();
            }
            default:
                throw new IllegalArgumentException("Unknown category: " + category);
        }
    }

    @JsonIgnore
    public boolean isAvailableToSell(Double lastPrice) {
        switch (category) {
            case SPOT: {
                var acquiredInUsdt = getAcquiredQty() * lastPrice;
                return getLastSuccessfulOrderHistoryItem(MAIN_FLOW)
                        .filter(orderContext -> orderContext.getSide().equals(BUY))
                        .isPresent() && acquiredInUsdt > FIVE_POINT_THREE;

            }
            case LINEAR: {
                var lastOrder = getLastSuccessfulOrderHistoryItem(MAIN_FLOW);
                return lastOrder.isEmpty() ||
                        lastOrder.filter(OrderContext::isLongOperationType).isPresent() ||
                        lastOrder.filter(OrderContext::isOlderThanLast12Hours).isPresent();
            }
            default:
                throw new IllegalArgumentException("Unknown category: " + category);
        }
    }

    @JsonIgnore
    public boolean isAvailableToOverTake(Double lastPrice) {
        var lastOrder = getLastSuccessfulOrderHistoryItem(MAIN_FLOW);
        var lastOvertakeOrder = getLastSuccessfulOrderHistoryItem(OVERTAKE_FLOW);
        return lastOvertakeOrder.isEmpty() ||
                lastOrder
                        .map(OrderContext::getOrderResultInfo)
                        .map(OrderResultInfo::getTime)
                        .filter(time -> time >
                                lastOvertakeOrder.map(OrderContext::getOrderResultInfo)
                                        .map(OrderResultInfo::getTime)
                                        .orElse(Long.MAX_VALUE))
                        .isPresent();
    }

    @JsonIgnore
    public boolean isAvailableToCloseShortPosition() {
        return getLastSuccessfulOrderHistoryItem(MAIN_FLOW)
                .filter(OrderContext::isShortOperationType)
                .filter(order -> !order.isClosed())
                .isPresent();
    }

    @JsonIgnore
    public boolean isAvailableToCloseLongPosition() {
        return getLastSuccessfulOrderHistoryItem(MAIN_FLOW)
                .filter(OrderContext::isLongOperationType)
                .filter(order -> !order.isClosed())
                .isPresent();
    }

}
