package com.crypto.sick.trade.service.action;

import com.bybit.api.client.domain.trade.Side;
import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.data.user.FlowState;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.service.MarketRepository;
import com.crypto.sick.trade.service.TradeOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class LinearTakeProfitActionProcessor implements TradeAction {

    private FlowTypeEnum flowType = FlowTypeEnum.TAKE_PROFIT_FLOW;

    @Autowired
    private TradeOperationService tradeOperationService;
    @Autowired
    private MarketRepository marketRepository;

    @Override
    public CoinIntervalTradingState processAction(CoinIntervalTradingState coinTradingState, CredentialsState credentials) {
        return Optional.ofNullable(coinTradingState.getFlowStates().get(flowType))
                .map(flowState -> processAction(flowState, coinTradingState, credentials))
                .orElse(coinTradingState);
    }

    private CoinIntervalTradingState processAction(FlowState flowState, CoinIntervalTradingState coinTradingState, CredentialsState credentials) {
        return switch (flowState.getStatus()) {
            case BUY -> buyAction(coinTradingState, credentials);
            case SELL -> sellAction(coinTradingState, credentials);
            case SLEEPING -> sleepingAction(coinTradingState, credentials);
            case PRE_SELL -> preSellAction(coinTradingState);
            case PRE_BUY -> preBuyAction(coinTradingState);
            case DISABLED -> coinTradingState;
            case STOP_LOSS -> coinTradingState;
        };
    }

    private CoinIntervalTradingState buyAction(CoinIntervalTradingState coinTradingState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        log.info("Take profit action for symbol: {}, side {}", symbol, Side.SELL);
        tradeOperationService.makeShortCloseOperation(symbol, coinTradingState, targetMarketState, credentials);
        return coinTradingState
                .closeLastSuccessfulPosition(Side.SELL)
                .forceStatus(flowType, TradingStrategyStatusEnum.SLEEPING);
    }

    private CoinIntervalTradingState sellAction(CoinIntervalTradingState coinTradingState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        log.info("Take profit action for symbol: {}, side {}", symbol, Side.BUY);
        tradeOperationService.makeLongCloseOperation(symbol, coinTradingState, targetMarketState, credentials);
        return coinTradingState
                .closeLastSuccessfulPosition(Side.BUY)
                .forceStatus(flowType, TradingStrategyStatusEnum.SLEEPING);
    }

    private CoinIntervalTradingState sleepingAction(CoinIntervalTradingState coinTradingState, CredentialsState credentials) {
        return coinTradingState;
    }

    private CoinIntervalTradingState preSellAction(CoinIntervalTradingState coinTradingState) {
        return coinTradingState;
    }

    private CoinIntervalTradingState preBuyAction(CoinIntervalTradingState coinTradingState) {
        return coinTradingState;
    }

}
