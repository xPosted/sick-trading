package com.crypto.sick.trade.service.action;

import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionRouter {

    @Autowired
    private SpotActionProcessor spotActionProcessor;
    @Autowired
    private LinearFlowActionRouter linearActionProcessor;

    public CoinIntervalTradingState processAction(CoinIntervalTradingState coinTradingState, CredentialsState credentials) {
        return switch (coinTradingState.getCategory()) {
            case SPOT ->
                    spotActionProcessor.processAction(coinTradingState, credentials);
            case LINEAR ->
                    linearActionProcessor.processAction(coinTradingState, credentials);
            default -> throw new IllegalArgumentException("Unsupported category: " + coinTradingState.getCategory());
        };
    }
}
