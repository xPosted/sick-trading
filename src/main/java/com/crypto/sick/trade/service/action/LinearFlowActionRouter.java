package com.crypto.sick.trade.service.action;

import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinearFlowActionRouter implements TradeAction {

    @Autowired
    private LinearActionProcessor linearActionProcessor;
    @Autowired
    private LinearTakeProfitActionProcessor linearTakeProfitActionProcessor;
    @Autowired
    private LinearHedgeActionProcessor linearHedgeActionProcessor;
    @Autowired
    private LinearChainActionProcessor linearChainActionProcessor;
    @Autowired
    private OvertakeActionProcessor overtakeActionProcessor;

    @Override
    public CoinIntervalTradingState processAction(CoinIntervalTradingState coinTradingState, CredentialsState credentials) {
        return List.of(linearActionProcessor, linearTakeProfitActionProcessor,
                        linearHedgeActionProcessor, linearChainActionProcessor,
                        overtakeActionProcessor).stream()
                .reduce(coinTradingState,
                        (state, processor) -> processor.processAction(state, credentials),
                        (s1, s2) -> s2);
    }
}
