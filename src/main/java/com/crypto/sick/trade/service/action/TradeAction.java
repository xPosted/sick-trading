package com.crypto.sick.trade.service.action;

import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;

public interface TradeAction {

    CoinIntervalTradingState processAction(CoinIntervalTradingState coinTradingState,
                                           CredentialsState credentials);

}
