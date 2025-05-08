package com.crypto.sick.trade.service;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.trade.Side;
import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.data.user.OrderContext;
import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.dto.state.MarketState;
import com.crypto.sick.trade.dto.web.bybit.PositionsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.bybit.api.client.domain.CategoryType.LINEAR;
import static com.bybit.api.client.domain.CategoryType.SPOT;
import static com.bybit.api.client.domain.trade.Side.BUY;
import static com.bybit.api.client.domain.trade.Side.SELL;
import static com.crypto.sick.trade.dto.enums.OrderOperationTypeEnum.*;
import static com.crypto.sick.trade.util.Utils.getPercentageOf;

@Slf4j
@Service
public class TradeOperationService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private AppConfig appConfig;

    public OrderContext makeBuyOperation(Symbol symbol, CoinIntervalTradingState coinTradingState, MarketState marketState, CredentialsState credentials) {
        var rsiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.MFI_STRATEGY);
        var lastPrice = marketState.getLastPrice();
        var orderResultInfo = orderService.placeMarketOrder(credentials, SPOT, symbol, BUY, String.valueOf(coinTradingState.getBuyAmount()), UUID.randomUUID().toString());
        var orderInfoResult = orderService.getOrderById(credentials, SPOT, orderResultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(orderResultInfo, orderInfoResult, rsiValue, mfiValue, lastPrice, BUY_OPERATION);
    }

    public OrderContext makeSellOperation(Symbol symbol, CoinIntervalTradingState coinTradingState, MarketState marketState, CredentialsState credentials) {
        var rsiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.MFI_STRATEGY);
        var lastPrice = marketState.getLastPrice();
        var resultQty = getPercentageOf(coinTradingState.getSellAmount(), coinTradingState.getAcquiredQty(), symbol.getSpotScale());
        var orderResultInfo = orderService.placeMarketOrder(credentials, SPOT, symbol, Side.SELL, resultQty.toString(), UUID.randomUUID().toString());
        log.info("Sell operation: " + orderResultInfo);
        var orderInfoResult = orderService.getOrderById(credentials, SPOT, orderResultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(orderResultInfo, orderInfoResult, rsiValue, mfiValue, lastPrice, SELL_OPERATION);
    }

    public OrderContext makeStopLossOperation(Symbol symbol, CoinIntervalTradingState coinTradingState, MarketState marketState, CredentialsState credentials, OrderContext stopLossOrder) {
        var qty = BigDecimal.valueOf(stopLossOrder.getAcquiredQty()).setScale(symbol.getSpotScale(), RoundingMode.DOWN);
        var rsiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.MFI_STRATEGY);
        var lastPrice = marketState.getLastPrice();
        var resultInfo = orderService.placeMarketOrder(credentials, SPOT, symbol, SELL, qty.toString(), UUID.randomUUID().toString());
        var orderInfoResult = orderService.getOrderById(credentials, SPOT, resultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(resultInfo, orderInfoResult, rsiValue, mfiValue, lastPrice, STOP_LOSS_OPERATION);
    }

    public OrderContext makeShortOperation(Symbol symbol, CoinIntervalTradingState coinTradingState, MarketState marketState, CredentialsState credentials) {
        orderService.setLeaverage(symbol, credentials, coinTradingState.getLeverage(), coinTradingState.getLeverage());
        var rsiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.MFI_STRATEGY);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var qty = BigDecimal.valueOf(coinTradingState.getBuyAmount()).divide(lastPrice, symbol.getLinearScale(), RoundingMode.DOWN);
        var takeProfitPrice = lastPrice.subtract(BigDecimal.valueOf(getPercentageOf(coinTradingState.getTakeProfit(), lastPrice.doubleValue(), lastPrice.scale())));
        var stopLossPrice = lastPrice.add(BigDecimal.valueOf(getPercentageOf(coinTradingState.getStopLoss(), lastPrice.doubleValue(), lastPrice.scale())));
        var orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, symbol, SELL, qty.toString(), "SHORT_OPEN_ORDER_" + new Random().nextInt(), takeProfitPrice.toString(), stopLossPrice.toString(), null, null);
        var orderInfoResult = orderService.getOrderById(credentials, LINEAR, orderResultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(orderResultInfo, orderInfoResult, rsiValue, mfiValue, lastPrice.doubleValue(), SHORT_OPERATION);
    }

    public OrderContext makeLongOperation(Symbol symbol, CoinIntervalTradingState coinTradingState, MarketState marketState, CredentialsState credentials) {
        orderService.setLeaverage(symbol, credentials, coinTradingState.getLeverage(), coinTradingState.getLeverage());
        var rsiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.MFI_STRATEGY);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var qty = BigDecimal.valueOf(coinTradingState.getBuyAmount()).divide(lastPrice, symbol.getLinearScale(), RoundingMode.DOWN);
        var takeProfitPrice = lastPrice.add(BigDecimal.valueOf(getPercentageOf(coinTradingState.getTakeProfit(), lastPrice.doubleValue(), lastPrice.scale())));
        var stopLossPrice = lastPrice.subtract(BigDecimal.valueOf(getPercentageOf(coinTradingState.getStopLoss(), lastPrice.doubleValue(), lastPrice.scale())));
        var orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, symbol, BUY, qty.toString(), "LONG_OPEN_ORDER_" + new Random().nextInt(), takeProfitPrice.toString(), stopLossPrice.toString(), null, null);
        var orderInfoResult = orderService.getOrderById(credentials, LINEAR, orderResultInfo.getOrderResponse().getOrderId());
        return OrderContext.buildOrderContext(orderResultInfo, orderInfoResult, rsiValue, mfiValue, lastPrice.doubleValue(), LONG_OPERATION);
    }

    public OrderContext makeLongCloseOperation(Symbol symbol, CoinIntervalTradingState coinTradingState, MarketState marketState, CredentialsState credentials) {
        var rsiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.MFI_STRATEGY);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, symbol, SELL, "0", "LONG_CLOSE_ORDER_" + new Random().nextInt(), null, null, true, true);
        return OrderContext.buildOrderContext(orderResultInfo, null, rsiValue, mfiValue, lastPrice.doubleValue(), LONG_CLOSE_OPERATION);
    }

    public OrderContext makeShortCloseOperation(Symbol symbol, CoinIntervalTradingState coinTradingState, MarketState marketState, CredentialsState credentials) {
        var rsiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.RSI_STRATEGY);
        var mfiValue = marketState.getIndicator(coinTradingState.getInterval(), StrategyEnum.MFI_STRATEGY);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var orderResultInfo = orderService.placeMarketOrder2(credentials, LINEAR, symbol, BUY, "0", "SHORT_CLOSE_ORDER_" + new Random().nextLong(), null, null, true, true);
        return OrderContext.buildOrderContext(orderResultInfo, null, rsiValue, mfiValue, lastPrice.doubleValue(), SHORT_CLOSE_OPERATION);
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


}
