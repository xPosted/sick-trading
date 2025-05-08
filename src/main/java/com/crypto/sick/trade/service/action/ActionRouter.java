package com.crypto.sick.trade.service.action;

import com.bybit.api.client.domain.CategoryType;
import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.service.strategy.StrategyEvaluationResult;
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
