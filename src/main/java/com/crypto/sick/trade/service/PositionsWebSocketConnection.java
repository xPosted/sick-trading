package com.crypto.sick.trade.service;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.crypto.sick.trade.data.user.CredentialsState;
import okhttp3.WebSocket;

import java.util.List;
import java.util.Optional;

public class PositionsWebSocketConnection {
    private WebSocket webSocket;
    private CredentialsState credentials;
    private String userName;

    public PositionsWebSocketConnection(String userName, CredentialsState credentials) {
        this.credentials = credentials;
        this.userName = userName;
    }

    public void positionWebSocketConnectionUpdate() {
        Optional.ofNullable(webSocket).ifPresent((w) -> w.close(4000, "RECONNECTION"));
        webSocket = null;
        int i = 0;
        while (i++ < 12 && webSocket == null) {
            updateConnection();
            try {
                Thread.sleep(i* 100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateConnection() {
        var client = BybitApiClientFactory.newInstance(credentials.getKey(), credentials.getSecret(),
                        "wss://stream-demo.bybit.com", false)
                .newWebsocketClient(20);
        client.setMessageHandler(message -> {
            System.out.println(String.format("Message 'position.linear' received, %s: %s", userName, message));

        });
        webSocket = client.getPrivateChannelStream(List.of("position.linear"), BybitApiConfig.V5_PRIVATE);
    }
}
