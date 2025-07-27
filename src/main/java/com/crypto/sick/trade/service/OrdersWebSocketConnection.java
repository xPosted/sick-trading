package com.crypto.sick.trade.service;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.trade.StopOrderType;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.dto.web.bybit.OrderStatusEnum;
import com.crypto.sick.trade.dto.web.bybit.ws.orders.WsOrderResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.WebSocket;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.crypto.sick.trade.util.Utils.getWebSocketUrl;

public class OrdersWebSocketConnection {
    private WebSocket webSocket;
    private CredentialsState credentials;
    private String userName;
    Consumer<List<WsOrderResponseDto.OrderData>> stopOrderHandler;

    public OrdersWebSocketConnection(String userName, CredentialsState credentials, Consumer<List<WsOrderResponseDto.OrderData>> stopOrderHandler) {
        this.credentials = credentials;
        this.userName = userName;
        this.stopOrderHandler = stopOrderHandler;
    }

    public void closeWebSocketConnection() {
        Optional.ofNullable(webSocket).ifPresent((w) -> w.close(4000, "RECONNECTION"));
        webSocket = null;
    }

    public void positionWebSocketConnectionUpdate() {
        closeWebSocketConnection();
        int i = 0;
        while (i++ < 12 && webSocket == null) {
            updateConnection();
            try {
                Thread.sleep(i * 100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateConnection() {
        var client = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(),
                        getWebSocketUrl(credentials.getBaseUrl()), false)
                .newWebsocketClient(20);
        client.setMessageHandler(message -> {
            var ordersInfo = (new ObjectMapper()).readValue(message, WsOrderResponseDto.class);
            if (ordersInfo.getData() == null || ordersInfo.getData().isEmpty()) {
                return;
            }
            var filledSlOrders = ordersInfo.getData().stream()
                    .filter(order -> Objects.equals(order.getOrderStatus(), OrderStatusEnum.FILLED))
                    .filter(WsOrderResponseDto.OrderData::isReduceOnly)
                    .filter(order -> Objects.equals(order.getStopOrderType(), StopOrderType.STOP_LOSS))
                    .toList();
            stopOrderHandler.accept(filledSlOrders);
          //  System.out.println(String.format("Message 'order.linear' received, %s: %s", userName, message));

        });
        webSocket = client.getPrivateChannelStream(List.of("order.linear"), BybitApiConfig.V5_PRIVATE);
    }
}
