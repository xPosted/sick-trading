package com.crypto.sick.trade.service.action;

import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.data.user.FlowState;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.service.MarketRepository;
import com.crypto.sick.trade.service.TradeOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum.SLEEPING;

@Service
public class OvertakeActionProcessor implements TradeAction {

    private FlowTypeEnum flowType = FlowTypeEnum.OVERTAKE_FLOW;

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
            case BUY -> buyAction(coinTradingState, flowState, credentials);
            case SELL -> sellAction(coinTradingState, flowState, credentials);
            case SLEEPING -> sleepingAction(coinTradingState);
            case DISABLED -> coinTradingState;
            case STOP_LOSS -> coinTradingState;
            default -> throw new IllegalStateException("Unexpected value: " + flowState.getStatus());
        };
    }

    private CoinIntervalTradingState buyAction(CoinIntervalTradingState coinTradingState, FlowState flowState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        var operationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, targetMarketState);
        var orderContext = tradeOperationService.makeLongOperation(operationContext);
        return coinTradingState.forceStatus(flowType, SLEEPING, orderContext);
    }

    private CoinIntervalTradingState sellAction(CoinIntervalTradingState coinTradingState, FlowState flowState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        var operationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, targetMarketState);
        var orderContext = tradeOperationService.makeShortOperation(operationContext);
        return coinTradingState.forceStatus(flowType, SLEEPING, orderContext);
    }

    private CoinIntervalTradingState sleepingAction(CoinIntervalTradingState coinTradingState) {
        return coinTradingState;
    }


}
