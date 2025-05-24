package com.crypto.sick.trade.service;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.position.PositionMode;
import com.bybit.api.client.domain.position.request.PositionDataRequest;
import com.bybit.api.client.domain.trade.OrderFilter;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.StopOrderType;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.bybit.api.client.domain.trade.response.OrderResult;
import com.bybit.api.client.restApi.BybitApiTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.data.user.OrderResultInfo;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.web.bybit.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.bybit.api.client.domain.TradeOrderType.MARKET;
import static com.crypto.sick.trade.data.user.OrderResultInfo.buildOrderResultInfo;
import static com.crypto.sick.trade.util.Utils.buildPlaceOrderResponseStub;
import static com.crypto.sick.trade.util.Utils.getPositionIds;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ObjectMapper objectMapper;

    public PlaceOrderResponse placeOrder(CredentialsState credentials, CategoryType category, Symbol symbol, Side side, TradeOrderType orderType, String qty, String price, String orderLinkId) {
        // Place order logic
        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl());
        BybitApiTradeRestClient client = factory.newTradeRestClient();
        Map<String, Object> order = Map.of(
                "category", category.getCategoryTypeId(),
                "symbol", symbol.getValue(),
                "side", side.getTransactionSide(),
                "orderType", orderType.getOType(),
                "qty", qty,
                "price", price,
                "orderLinkId", orderLinkId
        );
        if (appConfig.isDebugMode()) {
            log.info("------ Placing order:");
            log.info(order.toString());
            log.info("------ ");
            return new PlaceOrderResponse(new OrderResponse());
        }
        var response = client.createOrder(order);
        return objectMapper.convertValue(response, PlaceOrderResponse.class);
    }

    public OrderResultInfo placeMarketOrder(CredentialsState credentials, CategoryType category, Symbol symbol, Side side, String qty, String orderLinkId) {
        // Place order logic
        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl());
        BybitApiTradeRestClient client = factory.newTradeRestClient();
        Map<String, Object> order = Map.of(
                "category", category.getCategoryTypeId(),
                "symbol", symbol.getValue(),
                "side", side.getTransactionSide(),
                "orderType", MARKET.getOType(),
                "qty", qty,
                "orderLinkId", orderLinkId
        );
        if (appConfig.isDebugMode()) {
            log.info("------ Placing order:");
            log.info(order.toString());
            log.info("------ ");
            var stub = buildPlaceOrderResponseStub();
            return buildOrderResultInfo(stub, symbol, side, qty);
        }
        var response = client.createOrder(order);
        var placeOrderResponse = objectMapper.convertValue(response, PlaceOrderResponse.class);
        return buildOrderResultInfo(placeOrderResponse, symbol, side, qty);
    }

    public OrderResultInfo placeMarketOrder2(CredentialsState credentials,
                                             CategoryType category,
                                             Symbol symbol,
                                             Side side,
                                             String qty,
                                             String orderLinkId,
                                             String takeProfitPrice,
                                             String stopLossPrice,
                                             Boolean reduceOnly,
                                             Boolean closeOnTrigger) {
        return placeOrder(credentials, category, symbol, side, qty, MARKET, null, null, null,
                orderLinkId, takeProfitPrice, stopLossPrice, reduceOnly, closeOnTrigger);
    }

    public OrderResultInfo placeOrder(CredentialsState credentials,
                                      CategoryType category,
                                      Symbol symbol,
                                      Side side,
                                      String qty,
                                      TradeOrderType orderType,
                                      String price,
                                      String triggerPrice,
                                      Integer triggerDiraction,
                                      String orderLinkId,
                                      String takeProfitPrice,
                                      String stopLossPrice,
                                      Boolean reduceOnly,
                                      Boolean closeOnTrigger) {
        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl());
        BybitApiTradeRestClient client = factory.newTradeRestClient();
        TradeOrderRequest placeOrderRequest = TradeOrderRequest.builder()
                .category(category)
                .symbol(symbol.getValue())
                .side(side)
                .orderType(orderType)
                .price(price)
                .triggerPrice(triggerPrice)
                .triggerDirection(triggerDiraction)
                .qty(qty)
                .takeProfit(takeProfitPrice)
                .stopLoss(stopLossPrice)
                .orderLinkId(orderLinkId)
                .reduceOnly(reduceOnly)
                .closeOnTrigger(closeOnTrigger)
                .positionIdx(getPositionIds(side))
                .build();
        var response = client.createOrder(placeOrderRequest);
        var placeOrderResponse = objectMapper.convertValue(response, PlaceOrderResponse.class);
        if (placeOrderResponse.getRetCode() != 0) {
            log.error("Error placing order: {}", placeOrderResponse.getRetMsg());
        }
        return buildOrderResultInfo(placeOrderResponse, symbol, side, qty);
    }

    public void setLeaverage(Symbol symbol, CredentialsState credentials, Integer buyLeverage, Integer sellLeverage) {
        var client = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl()).newPositionRestClient();
        var setLeverageRequest = PositionDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(symbol.getValue())
                .buyLeverage(buyLeverage.toString())
                .sellLeverage(sellLeverage.toString())
                .build();
        client.setPositionLeverage(setLeverageRequest);
    }

    public OrderResult getOrderById(CredentialsState credentials, CategoryType categoryType, OrderResponse orderResponse) {
        var client = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl()).newTradeRestClient();
        var tradeHistoryRequest = TradeOrderRequest.builder()
                .category(categoryType)
                .orderId(orderResponse.getOrderId())
                .orderLinkId(orderResponse.getOrderLinkId())
                .limit(1).build();
        var rawResponse = client.getTradeHistory(tradeHistoryRequest);
        return objectMapper.convertValue(rawResponse, OrderResult.class);
    }

    //TODO: Add optional
    public OrderInfoResult getOrderById(CredentialsState credentials, CategoryType categoryType, String orderId) {
        if (orderId == null) {
            log.info("Order id is null -----");
            return null;
        }
        // toDO: remove this
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        var client = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl()).newTradeRestClient();
        var tradeHistoryRequest = TradeOrderRequest.builder()
                .category(categoryType)
                .orderId(orderId)
                .limit(1).build();
        if (appConfig.isDebugMode()) {
            return buildOrderInfoStub();
        }
        var rawResponse = client.getOrderHistory(tradeHistoryRequest);
        var responseWrapper = objectMapper.convertValue(rawResponse, OrderHistoryResponse.class);
        return responseWrapper.getResult();
    }

    public List<PositionsResponse.PositionDto> getOpenPositions(CredentialsState credentials, CategoryType categoryType, Symbol symbol) {
        var client = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl())
                .newPositionRestClient();
        var positionListRequest = PositionDataRequest.builder()
                .category(categoryType)
                .symbol(symbol.getValue())
                .build();
        var rawResponse = client.getPositionInfo(positionListRequest);
        var responseWrapper = objectMapper.convertValue(rawResponse, PositionsResponse.class);
        return Optional.ofNullable(responseWrapper.getResult())
                .map(PositionsResponse.PositionsCategoryWrapper::getPositions)
                .orElse(Collections.emptyList());
    }

    public void amendOrder(CredentialsState credentials, Long orderId, String price, String qty, CategoryType category, Symbol symbol) {
        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl());
        BybitApiTradeRestClient client = factory.newTradeRestClient();
        var amendOrderRequest = TradeOrderRequest.builder().orderId(String.valueOf(orderId)).category(CategoryType.SPOT).symbol(symbol.getValue())
                .price(price)  // setting a new price, for example
                .qty(qty)  // and a new quantity
                .build();
        var amendedOrder = client.amendOrder(amendOrderRequest);
        log.info(amendedOrder.toString());
    }

    public void cancelOrder(CredentialsState credentials, Long orderId, CategoryType category, Symbol symbol) {
        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl());
        BybitApiTradeRestClient client = factory.newTradeRestClient();
        var cancelOrderRequest = TradeOrderRequest.builder().category(category).symbol(symbol.getValue()).orderId(String.valueOf(orderId)).build();
        var canceledOrder = client.cancelOrder(cancelOrderRequest);
        log.info(canceledOrder.toString());
    }

    public void cancelAllOrders(CredentialsState credentials, CategoryType category, Symbol symbol) {
        BybitApiClientFactory factory = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl());
        BybitApiTradeRestClient client = factory.newTradeRestClient();
        var cancelAllOrdersRequest = TradeOrderRequest.builder()
                .category(category)
                .symbol(symbol.getValue())
                .orderFilter(OrderFilter.STOP_ORDER)
                .stopOrderType(StopOrderType.STOP)
                .build();
        log.info(client.cancelAllOrder(cancelAllOrdersRequest).toString());
    }

    public void getOpenOrders(CredentialsState credentials, CategoryType category, Symbol symbol) {
        var client = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl()).newTradeRestClient();
        var openLinearOrdersResult = client.getOpenOrders(TradeOrderRequest.builder().category(category).symbol(symbol.getValue()).openOnly(0).build());
        log.info(openLinearOrdersResult.toString());
    }

    public void switchPositionMode(CredentialsState credentials, CategoryType category, Symbol symbol, PositionMode positionMode) {
        var client = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(), credentials.getBaseUrl()).newPositionRestClient();
        var switchPositionMode = PositionDataRequest.builder()
                .category(category)
                .symbol(symbol.getValue())
                .positionMode(positionMode)
                .build();
        log.info("Switch position response: " + client.switchPositionMode(switchPositionMode));
    }

    private static OrderInfoResult buildOrderInfoStub() {
        return OrderInfoResult.builder()
                .orderEntries(List.of(buildStub()))
                .build();
    }

    private static OrderInfoEntry buildStub() {
        return OrderInfoEntry.builder()
                .orderId("123")
                .orderLinkId("123")
                .blockTradeId("123")
                .symbol("BTCUSD")
                .price("123")
                .qty("123")
                .side("BUY")
                .isLeverage("123")
                .orderStatus(null)
                .rejectReason(null)
                .avgPrice("123")
                .leavesQty("123")
                .leavesValue("123")
                .cumExecQty(1D)
                .cumExecValue("123")
                .cumExecFee(123)
                .timeInForce(null)
                .orderType("123")
                .takeProfit("123")
                .stopLoss("123")
                .tpslMode("123")
                .tpLimitPrice("123")
                .slLimitPrice("123")
                .lastPriceOnCreated("123")
                .createdTime("123")
                .updatedTime("123")
                .build();
    }

}
