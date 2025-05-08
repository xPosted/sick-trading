package com.crypto.sick.trade.service;

import com.bybit.api.client.domain.websocket_message.public_channel.PublicTickerData;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.state.MarketState;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
public class MarketRepository {

    private final Map<String, MarketState> marketStates = Collections.synchronizedMap(new HashMap<>());

    public synchronized MarketState getMarketState(Symbol symbol) {
        var result = marketStates.get(symbol.getValue());
        if (result == null) {
            return updateMarketState(MarketState.buildEmpty(symbol));
        }
        return result;
    }

    public Stream<MarketState> getMarketStates() {
        return marketStates.values().stream();
    }

    public void updateLastPrice(PublicTickerData publicTickerData) {
        marketStates.get(publicTickerData.getSymbol())
                    .setLastPrice(Double.parseDouble(publicTickerData.getLastPrice()));
    }

    private MarketState updateMarketState(MarketState marketState) {
        marketStates.put(marketState.getSymbol().getValue(), marketState);
        return marketState;
    }

    public Set<String> getSymbols() {
        return marketStates.keySet();
    }

}
