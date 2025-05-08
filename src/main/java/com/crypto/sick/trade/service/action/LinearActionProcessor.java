package com.crypto.sick.trade.service.action;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.trade.Side;
import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.data.user.FlowState;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.service.MarketRepository;
import com.crypto.sick.trade.service.TradeOperationService;
import com.crypto.sick.trade.service.strategy.StrategyEvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum.SLEEPING;

@Slf4j
@Service
public class LinearActionProcessor implements TradeAction {

    private FlowTypeEnum flowType = FlowTypeEnum.MAIN_FLOW;

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
        if (validateExistentOrders(credentials, symbol, Side.BUY, targetMarketState.getLastPrice())) {
            tradeOperationService.makeShortCloseOperation(symbol, coinTradingState, targetMarketState, credentials);
            var orderContext = tradeOperationService.makeLongOperation(symbol, coinTradingState, targetMarketState, credentials);
            return coinTradingState.forceStatus(flowType, SLEEPING, orderContext);
        }
        return coinTradingState;
    }

    private CoinIntervalTradingState sellAction(CoinIntervalTradingState coinTradingState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        if (validateExistentOrders(credentials, symbol, Side.SELL, targetMarketState.getLastPrice())) {
            tradeOperationService.makeLongCloseOperation(symbol, coinTradingState, targetMarketState, credentials);
            var orderContext =  tradeOperationService.makeShortOperation(symbol, coinTradingState, targetMarketState, credentials);
            return coinTradingState.forceStatus(flowType, SLEEPING, orderContext);

        }
        return coinTradingState;
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

    private boolean validateExistentOrders(CredentialsState credentials, Symbol symbol, Side side, double currentPrice) {
        switch (side) {
            case BUY -> {
                var positions = tradeOperationService.getOpenPositions(credentials, CategoryType.LINEAR, symbol, Side.BUY);
                var validationResult = positions.isEmpty() || positions.stream()
                        .allMatch(p -> p.getAvgPrice() > currentPrice);
                log.info("------- Validating existent orders for {}, side {}, allowed - {}", symbol, side, validationResult);
                return validationResult;
            }
            case SELL -> {
                var positions = tradeOperationService.getOpenPositions(credentials, CategoryType.LINEAR, symbol, Side.SELL);
                var validationResult = positions.isEmpty() || positions.stream()
                        .allMatch(p -> p.getAvgPrice() < currentPrice);
                log.info("------- Validating existent orders for {}, side {}, allowed - {}", symbol, side, validationResult);
                return validationResult;
            }
            default -> throw new IllegalStateException("Unexpected value: " + side);
        }
    }
}
