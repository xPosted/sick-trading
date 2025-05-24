package com.crypto.sick.trade.service.action;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.trade.Side;
import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.data.user.FlowState;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.dto.state.MarketState;
import com.crypto.sick.trade.service.MarketRepository;
import com.crypto.sick.trade.service.TradeOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class LinearHedgeActionProcessor implements TradeAction {

    private FlowTypeEnum flowType = FlowTypeEnum.HEDGE_FLOW;

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
            case SLEEPING -> coinTradingState;
            case PRE_SELL -> coinTradingState;
            case PRE_BUY -> coinTradingState;
            case DISABLED -> coinTradingState;
            case STOP_LOSS -> coinTradingState;
            default -> throw new IllegalStateException("Unexpected value: " + flowState.getStatus());
        };
    }

    private CoinIntervalTradingState buyAction(CoinIntervalTradingState coinTradingState, FlowState flowState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        if (validateExistentOrders(credentials, symbol, Side.BUY, targetMarketState.getLastPrice())) {
            var operationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, targetMarketState);
            var orderContext = tradeOperationService.makeLongOperation(operationContext);
            return coinTradingState.forceStatus(flowType, TradingStrategyStatusEnum.PRE_BUY, orderContext);
        }
        return coinTradingState;
    }

    private CoinIntervalTradingState sellAction(CoinIntervalTradingState coinTradingState, FlowState flowState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        if (validateExistentOrders(credentials, symbol, Side.SELL, targetMarketState.getLastPrice())) {
            var operationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, targetMarketState);
            var orderContext = tradeOperationService.makeShortOperation(operationContext);
            return coinTradingState.forceStatus(flowType, TradingStrategyStatusEnum.PRE_SELL, orderContext);
        }
        return coinTradingState;
    }

    private boolean validateExistentOrders(CredentialsState credentials, Symbol symbol, Side side, double currentPrice) {
        switch (side) {
            case BUY -> {
                var positions = tradeOperationService.getOpenPositions(credentials, CategoryType.LINEAR, symbol, Side.BUY);
                var validationResult = positions.isEmpty();
                //         || positions.stream().allMatch(p -> p.getAvgPrice() > currentPrice);
                log.info("------- Validating existent orders for {}, side {}, allowed - {}", symbol, side, validationResult);
                return validationResult;
            }
            case SELL -> {
                var positions = tradeOperationService.getOpenPositions(credentials, CategoryType.LINEAR, symbol, Side.SELL);
                var validationResult = positions.isEmpty();
                //     || positions.stream().allMatch(p -> p.getAvgPrice() < currentPrice);
                log.info("------- Validating existent orders for {}, side {}, allowed - {}", symbol, side, validationResult);
                return validationResult;
            }
            default -> throw new IllegalStateException("Unexpected value: " + side);
        }
    }

}
