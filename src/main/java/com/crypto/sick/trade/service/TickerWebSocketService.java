package com.crypto.sick.trade.service;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.websocket_message.public_channel.WebSocketTickerMessage;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.WebSocket;

import java.util.List;
import java.util.Optional;

public class TickerWebSocketService {

    private WebSocket webSocket;
    private MarketRepository marketRepository;
    private List<Symbol> symbols;

    public TickerWebSocketService(MarketRepository marketRepository, List<Symbol> symbols) {
        this.marketRepository = marketRepository;
        this.symbols = symbols;
    }

    public void tickerWebSocketConnectionUpdate() {
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
        var client = BybitApiClientFactory.newInstance(BybitApiConfig.STREAM_MAINNET_DOMAIN, false).newWebsocketClient(20);
        client.setMessageHandler(message -> {
            var tickerData = (new ObjectMapper()).readValue(message, WebSocketTickerMessage.class);
            Optional.ofNullable(tickerData.getData())
                    .ifPresent(marketRepository::updateLastPrice);
        });
        webSocket = client.getPublicChannelStream(getTopics(), BybitApiConfig.V5_PUBLIC_SPOT);
    }

    private List<String> getTopics() {
        return symbols.stream()
                .map(symbol -> "tickers." + symbol.getValue())
                .toList();
    }

}
