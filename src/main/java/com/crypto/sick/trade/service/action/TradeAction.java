package com.crypto.sick.trade.service.action;

import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.service.strategy.StrategyEvaluationResult;

public interface TradeAction {

    CoinIntervalTradingState processAction(CoinIntervalTradingState coinTradingState,
                                           CredentialsState credentials);

}
