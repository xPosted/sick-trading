package com.crypto.sick.trade.service.action;

import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.data.user.FlowState;
import com.crypto.sick.trade.data.user.OrderContext;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.dto.state.MarketState;
import com.crypto.sick.trade.service.MarketRepository;
import com.crypto.sick.trade.service.TradeOperationService;
import com.crypto.sick.trade.service.strategy.StrategyEvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Flow;

import static com.bybit.api.client.domain.trade.Side.BUY;
import static com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum.*;
import static com.crypto.sick.trade.util.Utils.calculateDiffPrcntAbs;

@Slf4j
@Service
public class SpotActionProcessor implements TradeAction {

    private FlowTypeEnum flowType = FlowTypeEnum.MAIN_FLOW;

    @Autowired
    private TradeOperationService tradeOperationService;
    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private AppConfig appConfig;

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
            case SLEEPING -> sleepingAction(coinTradingState, flowState, credentials);
            case PRE_SELL -> preSellAction(coinTradingState);
            case PRE_BUY -> preBuyAction(coinTradingState);
            case DISABLED -> coinTradingState;
            case STOP_LOSS -> coinTradingState;
            default -> throw new IllegalStateException("Unexpected value: " + flowState.getStatus());
        };
    }

    private CoinIntervalTradingState buyAction(CoinIntervalTradingState coinTradingState, FlowState flowState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        var operationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, targetMarketState);
        var orderContext = tradeOperationService.makeBuyOperation(operationContext);
        if (orderContext.isSuccessful()) {
            return coinTradingState.forceStatus(flowType, SLEEPING, orderContext);
        }
        return coinTradingState.withOrder(orderContext);
    }

    private CoinIntervalTradingState sellAction(CoinIntervalTradingState coinTradingState, FlowState flowState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        var operationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, targetMarketState);
        var orderContext = tradeOperationService.makeSellOperation(operationContext);
        if (orderContext.isSuccessful()) {
            return coinTradingState.forceStatus(flowType, SLEEPING, orderContext);
        }
        return coinTradingState.withOrder(orderContext);
    }

    private CoinIntervalTradingState sleepingAction(CoinIntervalTradingState coinTradingState, FlowState flowState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        var stopLossOrderOptional = checkStopLossTrigger(coinTradingState, flowState, targetMarketState);
        if (stopLossOrderOptional.isPresent()) {
            // make sell
            var stopLossOrder = stopLossOrderOptional.get();
            var orderContext = tradeOperationService.makeStopLossOperation(flowType, symbol, coinTradingState, targetMarketState, credentials, stopLossOrder);
            if (appConfig.isDisableOnStopLoss()) {
                return coinTradingState.forceStatus(flowType, DISABLED, orderContext);
            }
            return coinTradingState.forceStatus(flowType, STOP_LOSS, orderContext);
        }
        return coinTradingState;
    }

    private CoinIntervalTradingState preSellAction(CoinIntervalTradingState coinTradingState) {
        return coinTradingState;
    }

    private CoinIntervalTradingState preBuyAction(CoinIntervalTradingState coinTradingState) {
        return coinTradingState;
    }

    private Optional<OrderContext> checkStopLossTrigger(CoinIntervalTradingState coinTradingState, FlowState flowState, MarketState marketState) {
        Optional<OrderContext> optionalLastOrder = coinTradingState.getLastSuccessfulOrderHistoryItem(flowType);
        if (optionalLastOrder.isPresent()) {
            OrderContext lastOrder = optionalLastOrder.get();
            if (lastOrder.getOrderResultInfo().getSide() == BUY) {
                double diffPrcnt = calculateDiffPrcntAbs(lastOrder.getLastPrice(), marketState.getLastPrice());
                if (lastOrder.getLastPrice() > marketState.getLastPrice() && diffPrcnt > flowState.getStopLoss()) {
                    log.info("Stop loss triggered, last buying price: " + lastOrder.getLastPrice() + " current price: " + marketState.getLastPrice() + " diff: " + diffPrcnt);
                    return Optional.of(lastOrder);
                }
            }
        }
        return Optional.empty();
    }
}
