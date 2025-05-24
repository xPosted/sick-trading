package com.crypto.sick.trade.service;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.position.PositionMode;
import com.bybit.api.client.domain.trade.Side;
import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.data.user.FlowState;
import com.crypto.sick.trade.data.user.OrderContext;
import com.crypto.sick.trade.dto.enums.*;
import com.crypto.sick.trade.dto.state.MarketState;
import com.crypto.sick.trade.dto.web.bybit.PositionsResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.bybit.api.client.domain.CategoryType.LINEAR;
import static com.bybit.api.client.domain.CategoryType.SPOT;
import static com.bybit.api.client.domain.TradeOrderType.LIMIT;
import static com.bybit.api.client.domain.trade.Side.BUY;
import static com.bybit.api.client.domain.trade.Side.SELL;
import static com.crypto.sick.trade.dto.enums.OrderOperationTypeEnum.*;
import static com.crypto.sick.trade.dto.enums.TriggerDirectionEnum.FALLS_TO;
import static com.crypto.sick.trade.dto.enums.TriggerDirectionEnum.RISES_TO;
import static com.crypto.sick.trade.util.Utils.getPercentageOf;

@Slf4j
@Service
public class TradeOperationService {

    @Autowired
    private OrderService orderService;

    public OrderContext makeBuyOperation(OperationContext operationContext) {
        var symbol = operationContext.getSymbol();
        var interval = operationContext.getTaapiInterval();
        var credentials = operationContext.getCredentials();
        var marketState = operationContext.getMarketState();
        var buyAmount = operationContext.getBuyAmount();
        var rsiValue = marketState.getIndicator(interval, StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(interval, StrategyEnum.MFI_STRATEGY);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var flowType = operationContext.getFlowType();

        var orderResultInfo = orderService.placeMarketOrder(credentials, SPOT, symbol, BUY, String.valueOf(buyAmount), UUID.randomUUID().toString());
        var orderInfoResult = orderService.getOrderById(credentials, SPOT, orderResultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(flowType, orderResultInfo, orderInfoResult, rsiValue, mfiValue, lastPrice.doubleValue(), BUY_OPERATION);
    }

    public OrderContext makeSellOperation(OperationContext operationContext) {
        var symbol = operationContext.getSymbol();
        var interval = operationContext.getTaapiInterval();
        var credentials = operationContext.getCredentials();
        var marketState = operationContext.getMarketState();
        var sellAmount = operationContext.getSellAmount();
        var acquiredQty = operationContext.getAcquiredQty();
        var rsiValue = marketState.getIndicator(interval, StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(interval, StrategyEnum.MFI_STRATEGY);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var flowType = operationContext.getFlowType();

        var resultQty = getPercentageOf(sellAmount, acquiredQty, symbol.getSpotScale());
        var orderResultInfo = orderService.placeMarketOrder(credentials, SPOT, symbol, Side.SELL, resultQty.toString(), UUID.randomUUID().toString());
        log.info("Sell operation: " + orderResultInfo);
        var orderInfoResult = orderService.getOrderById(credentials, SPOT, orderResultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(flowType, orderResultInfo, orderInfoResult, rsiValue, mfiValue, lastPrice.doubleValue(), SELL_OPERATION);
    }

    public OrderContext makeStopLossOperation(FlowTypeEnum flowType, Symbol symbol, CoinIntervalTradingState coinTradingState, MarketState marketState, CredentialsState credentials, OrderContext stopLossOrder) {
        var qty = BigDecimal.valueOf(stopLossOrder.getAcquiredQty()).setScale(symbol.getSpotScale(), RoundingMode.DOWN);
        var rsiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.MFI_STRATEGY);
        var lastPrice = marketState.getLastPrice();
        var resultInfo = orderService.placeMarketOrder(credentials, SPOT, symbol, SELL, qty.toString(), UUID.randomUUID().toString());
        var orderInfoResult = orderService.getOrderById(credentials, SPOT, resultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(flowType, resultInfo, orderInfoResult, rsiValue, mfiValue, lastPrice, STOP_LOSS_OPERATION);
    }

    public OrderContext makeShortOperation(OperationContext operationContext) {
        var symbol = operationContext.getSymbol();
        var interval = operationContext.getTaapiInterval();
        var credentials = operationContext.getCredentials();
        var leverage = operationContext.getLeverage();
        var tp = Optional.ofNullable(operationContext.getTp());
        var sl =  Optional.ofNullable(operationContext.getSl());
        var marketState = operationContext.getMarketState();
        var buyAmount = operationContext.getBuyAmount();
        var rsiValue = marketState.getIndicator(interval, StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(interval, StrategyEnum.MFI_STRATEGY);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var flowType = operationContext.getFlowType();

        setHedgePositionMode(credentials, LINEAR, symbol);
        orderService.setLeaverage(symbol, credentials, leverage, leverage);
        var qty = BigDecimal.valueOf(buyAmount).divide(lastPrice, symbol.getLinearScale(), RoundingMode.DOWN);
        var takeProfitPrice = tp
                .filter(tpValue -> tpValue > 0)
                .map(tpValue -> lastPrice.subtract(BigDecimal.valueOf(getPercentageOf(tpValue, lastPrice.doubleValue(), lastPrice.scale()))))
                .map(BigDecimal::toString)
                .orElse(null);
        var stopLossPrice = sl
                .filter(slValue -> slValue > 0)
                .map(slValue -> lastPrice.add(BigDecimal.valueOf(getPercentageOf(slValue, lastPrice.doubleValue(), lastPrice.scale()))))
                .map(BigDecimal::toString)
                .orElse(null);
        log.info("Take profit: " + takeProfitPrice + " Stop loss: " + stopLossPrice + "tp: " + tp + " sl: " + sl);
        var orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, symbol, SELL, qty.toString(), "SHORT_OPEN_ORDER_" + new Random().nextInt(), takeProfitPrice, stopLossPrice, null, null);
        var orderInfoResult = orderService.getOrderById(credentials, LINEAR, orderResultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(flowType, orderResultInfo, orderInfoResult, rsiValue, mfiValue, lastPrice.doubleValue(), SHORT_OPERATION);
    }

    public OrderContext makeShortLimitConditionalOperation(OperationContext operationContext) {
        var symbol = operationContext.getSymbol();
        var interval = operationContext.getTaapiInterval();
        var credentials = operationContext.getCredentials();
        var leverage = operationContext.getLeverage();
        var tp = operationContext.getTp();
        var sl = operationContext.getSl();
        var marketState = operationContext.getMarketState();
        var buyAmount = operationContext.getBuyAmount();
        var rsiValue = marketState.getIndicator(interval, StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(interval, StrategyEnum.MFI_STRATEGY);
        var marketPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var flowType = operationContext.getFlowType();
        var triggerPrice = BigDecimal.valueOf(operationContext.getPrice());

        setHedgePositionMode(credentials, LINEAR, symbol);
        orderService.setLeaverage(symbol, credentials, leverage, leverage);
        var qty = BigDecimal.valueOf(buyAmount).divide(marketPrice, symbol.getLinearScale(), RoundingMode.DOWN);
        var takeProfitPrice = triggerPrice.subtract(BigDecimal.valueOf(getPercentageOf(tp, marketPrice.doubleValue(), marketPrice.scale())));
        var stopLossPrice = triggerPrice.add(BigDecimal.valueOf(getPercentageOf(sl, marketPrice.doubleValue(), marketPrice.scale())));

        var orderResultInfo = orderService.placeOrder(credentials, LINEAR, symbol, SELL, qty.toString(), LIMIT, triggerPrice.toString(), triggerPrice.toString(), FALLS_TO.getValue(), "SHORT_OPEN_ORDER_" + new Random().nextInt(), takeProfitPrice.toString(), stopLossPrice.toString(), null, null);
        var orderInfoResult = orderService.getOrderById(credentials, LINEAR, orderResultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(flowType, orderResultInfo, orderInfoResult, rsiValue, mfiValue, marketPrice.doubleValue(), SHORT_OPERATION);
    }

    public OrderContext makeLongLimitConditionalOperation(OperationContext operationContext) {
        var symbol = operationContext.getSymbol();
        var interval = operationContext.getTaapiInterval();
        var credentials = operationContext.getCredentials();
        var leverage = operationContext.getLeverage();
        var tp = operationContext.getTp();
        var sl = operationContext.getSl();
        var marketState = operationContext.getMarketState();
        var buyAmount = operationContext.getBuyAmount();
        var rsiValue = marketState.getIndicator(interval, StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(interval, StrategyEnum.MFI_STRATEGY);
        var marketPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var flowType = operationContext.getFlowType();
        var triggerPrice = BigDecimal.valueOf(operationContext.getPrice());

        setHedgePositionMode(credentials, LINEAR, symbol);
        orderService.setLeaverage(symbol, credentials, leverage, leverage);
        var qty = BigDecimal.valueOf(buyAmount).divide(marketPrice, symbol.getLinearScale(), RoundingMode.DOWN);
        var takeProfitPrice = triggerPrice.add(BigDecimal.valueOf(getPercentageOf(tp, marketPrice.doubleValue(), marketPrice.scale())));
        var stopLossPrice = triggerPrice.subtract(BigDecimal.valueOf(getPercentageOf(sl, marketPrice.doubleValue(), marketPrice.scale())));
        var orderResultInfo = orderService.placeOrder(credentials, LINEAR, symbol, BUY, qty.toString(), LIMIT, triggerPrice.toString(), triggerPrice.toString(), RISES_TO.getValue(), "LONG_OPEN_ORDER_" + new Random().nextInt(), takeProfitPrice.toString(), stopLossPrice.toString(), null, null);
        var orderInfoResult = orderService.getOrderById(credentials, LINEAR, orderResultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(flowType, orderResultInfo, orderInfoResult, rsiValue, mfiValue, marketPrice.doubleValue(), LONG_OPERATION);
    }



    public void cancelAllOrders(CredentialsState credentials, CategoryType categoryType, Symbol symbol) {
        orderService.cancelAllOrders(credentials, categoryType, symbol);
    }

    public OrderContext makeLongOperation(OperationContext operationContext) {
        var symbol = operationContext.getSymbol();
        var interval = operationContext.getTaapiInterval();
        var credentials = operationContext.getCredentials();
        var leverage = operationContext.getLeverage();
        var tp = Optional.ofNullable(operationContext.getTp());
        var sl =  Optional.ofNullable(operationContext.getSl());
        var marketState = operationContext.getMarketState();
        var buyAmount = operationContext.getBuyAmount();
        var rsiValue = marketState.getIndicator(interval, StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(interval, StrategyEnum.MFI_STRATEGY);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var flowType = operationContext.getFlowType();

        setHedgePositionMode(credentials, LINEAR, symbol);
        orderService.setLeaverage(symbol, credentials, leverage, leverage);
        var qty = BigDecimal.valueOf(buyAmount).divide(lastPrice, symbol.getLinearScale(), RoundingMode.DOWN);
        var takeProfitPrice = tp
                .filter(tpValue -> tpValue > 0)
                .map(tpValue -> lastPrice.add(BigDecimal.valueOf(getPercentageOf(tpValue, lastPrice.doubleValue(), lastPrice.scale()))))
                .map(BigDecimal::toString)
                .orElse(null);
        var stopLossPrice = sl
                .filter(slValue -> slValue > 0)
                .map(slValue ->  lastPrice.subtract(BigDecimal.valueOf(getPercentageOf(slValue, lastPrice.doubleValue(), lastPrice.scale()))))
                .map(BigDecimal::toString)
                .orElse(null);
        log.info("Take profit: " + takeProfitPrice + " Stop loss: " + stopLossPrice + "tp: " + tp + " sl: " + sl);
        var orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, symbol, BUY, qty.toString(), "LONG_OPEN_ORDER_" + new Random().nextInt(), takeProfitPrice, stopLossPrice, null, null);
        var orderInfoResult = orderService.getOrderById(credentials, LINEAR, orderResultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(flowType, orderResultInfo, orderInfoResult, rsiValue, mfiValue, lastPrice.doubleValue(), LONG_OPERATION);
    }

    public OrderContext makeLongCloseOperation(OperationContext operationContext) {
        var symbol = operationContext.getSymbol();
        var interval = operationContext.getTaapiInterval();
        var credentials = operationContext.getCredentials();
        var marketState = operationContext.getMarketState();
        var rsiValue = marketState.getIndicator(interval, StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(interval, StrategyEnum.MFI_STRATEGY);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var flowType = operationContext.getFlowType();

        var orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, symbol, SELL, "0", "LONG_CLOSE_ORDER_" + new Random().nextInt(), null, null, true, true);
        return OrderContext.buildOrderContext(flowType, orderResultInfo, null, rsiValue, mfiValue, lastPrice.doubleValue(), LONG_CLOSE_OPERATION);
    }

    public OrderContext makeShortCloseOperation(OperationContext operationContext) {
        var symbol = operationContext.getSymbol();
        var interval = operationContext.getTaapiInterval();
        var credentials = operationContext.getCredentials();
        var marketState = operationContext.getMarketState();
        var rsiValue = marketState.getIndicator(interval, StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(interval, StrategyEnum.MFI_STRATEGY);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var flowType = operationContext.getFlowType();

        var orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, symbol, BUY, "0", "SHORT_CLOSE_ORDER_" + new Random().nextLong(), null, null, true, true);
        return OrderContext.buildOrderContext(flowType, orderResultInfo, null, rsiValue, mfiValue, lastPrice.doubleValue(), SHORT_CLOSE_OPERATION);
    }

    public void setHedgePositionMode(CredentialsState credentials, CategoryType categoryType, Symbol symbol) {
        orderService.switchPositionMode(credentials, categoryType, symbol, PositionMode.BOTH_SIDES);
    }

    public void testShortLinearOrder(CredentialsState credentials) {
        orderService.setLeaverage(Symbol.SCAUSDT, credentials, 2,2);
        var orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, Symbol.SCAUSDT, SELL, "70", "TEST_LINEAR_ORDER_" + new Random().nextInt(), "0.07", "0.083", null, null);
        log.info("Test order result: " + orderResultInfo);
        orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, Symbol.SCAUSDT, BUY, "0", "TEST_LINEAR_ORDER_" + new Random().nextInt(), null, null, true, true);
        log.info("Test order result: " + orderResultInfo);
    }

    public void testLongLinearOrder(CredentialsState credentials) {
        orderService.setLeaverage(Symbol.SCAUSDT, credentials, 2,2);
        var orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, Symbol.SCAUSDT, SELL, "5", "TEST_LINEAR_ORDER_" + new Random().nextInt(), "0.07", "0.083", null, null);
        log.info("Test order result: " + orderResultInfo);
        orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, Symbol.SCAUSDT, BUY, "0", "TEST_LINEAR_ORDER_" + new Random().nextInt(), null, null, true, true);
        log.info("Test order result: " + orderResultInfo);
    }

    public List<PositionsResponse.PositionDto> getOpenPositions(CredentialsState credentials, CategoryType categoryType, Symbol symbol, Side side) {
        var positions = orderService.getOpenPositions(credentials, categoryType, symbol);
        return positions.stream().filter(p -> positionsFilter(p, side)).collect(Collectors.toList());
    }

    public void getOpenOrders(CredentialsState credentials) {
        orderService.getOpenOrders(credentials, LINEAR, Symbol.SCAUSDT);
    }

    private boolean positionsFilter(PositionsResponse.PositionDto position, Side side) {
        return position.getSide() != null
                && ! position.getSide().isBlank()
                && position.getPositionValue() != null
                && ! position.getPositionValue().isBlank()
                && position.getSide().equalsIgnoreCase(side.getTransactionSide())
                && position.getAvgPrice() > 0;
    }

    @Value
    @Builder(toBuilder = true)
    @AllArgsConstructor
    public static class OperationContext {

        Symbol symbol;
        FlowTypeEnum flowType;
        MarketState marketState;
        TaapiIntervalEnum taapiInterval;
        Integer leverage;
        Double tp;
        Double sl;
        Double price;
        TradeOrderType orderType;
        CredentialsState credentials;
        Double buyAmount;
        Double sellAmount;
        Double acquiredQty;

        public OperationContext(CoinIntervalTradingState coinIntervalTradingState, FlowState targetFlow, CredentialsState credentials, MarketState marketState) {
            this(coinIntervalTradingState, targetFlow, credentials, marketState, TradeOrderType.MARKET, null);
        }

        public OperationContext(CoinIntervalTradingState coinIntervalTradingState, FlowState targetFlow, CredentialsState credentials, MarketState marketState, TradeOrderType orderType, Double price) {
            this.symbol = coinIntervalTradingState.getSymbol();
            this.flowType = targetFlow.getFlowType();
            this.taapiInterval = coinIntervalTradingState.getInterval();
            this.leverage = coinIntervalTradingState.getLeverage();
            this.tp = targetFlow.getTakeProfit();
            this.sl = targetFlow.getStopLoss();
            this.credentials = credentials;
            this.buyAmount = coinIntervalTradingState.getBuyAmount();
            this.sellAmount = coinIntervalTradingState.getSellAmount();
            this.acquiredQty = coinIntervalTradingState.getAcquiredQty();
            this.marketState = marketState;
            this.orderType = orderType;
            this.price = price;
        }

    }


}
