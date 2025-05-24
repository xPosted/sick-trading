package com.crypto.sick.trade.service.action;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.data.user.CredentialsState;
import com.crypto.sick.trade.data.user.FlowState;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.service.MarketRepository;
import com.crypto.sick.trade.service.TradeOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.crypto.sick.trade.dto.enums.StrategyEnum.CHAIN_STRATEGY;
import static com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum.SLEEPING;
import static com.crypto.sick.trade.util.Utils.getPercentageOf;

@Service
public class LinearChainActionProcessor implements TradeAction {

    private FlowTypeEnum flowType = FlowTypeEnum.CHAIN_FLOW;

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
            case SETUP ->processSetup(flowState, coinTradingState, credentials);
            case BUY -> processBuy(flowState, coinTradingState, credentials);
            case SELL -> processSell(flowState, coinTradingState, credentials);
            default -> coinTradingState;
        };
    }

    private CoinIntervalTradingState processSetup(FlowState flowState, CoinIntervalTradingState coinTradingState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var strategyState = flowState.getStrategies().get(CHAIN_STRATEGY);
        var priceOffset = strategyState.getPriceOffset();
        var marketState = marketRepository.getMarketState(symbol);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());

        var longTriggerPrice = lastPrice
                .add(BigDecimal.valueOf(getPercentageOf(priceOffset, lastPrice.doubleValue(), lastPrice.scale())))
                .doubleValue();

        var shortTriggerPrice = lastPrice
                .subtract(BigDecimal.valueOf(getPercentageOf(priceOffset, lastPrice.doubleValue(), lastPrice.scale())))
                .doubleValue();

        tradeOperationService.cancelAllOrders(credentials, CategoryType.LINEAR, symbol);
        var longFutureOrderContext = CompletableFuture.supplyAsync(() -> {var longOperationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, marketState, TradeOrderType.LIMIT, longTriggerPrice);
            return tradeOperationService.makeLongLimitConditionalOperation(longOperationContext);
        });

        var shortFutureOperationContext = CompletableFuture.supplyAsync(() -> {var shortOperationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, marketState, TradeOrderType.LIMIT, shortTriggerPrice);
            return tradeOperationService.makeShortLimitConditionalOperation(shortOperationContext);
        });

//        var longOperationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, marketState, TradeOrderType.LIMIT, longTriggerPrice);
//        var longOrderContext = tradeOperationService.makeLongLimitConditionalOperation(longOperationContext);
//
//        var shortOperationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, marketState, TradeOrderType.LIMIT, shortTriggerPrice);
//        var shortOrderContext = tradeOperationService.makeShortLimitConditionalOperation(shortOperationContext);
        try {
            return coinTradingState.forceStatus(flowType, SLEEPING, longFutureOrderContext.get())
                    .forceStatus(flowType, SLEEPING, shortFutureOperationContext.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private CoinIntervalTradingState processSell(FlowState flowState, CoinIntervalTradingState coinTradingState, CredentialsState credentials) {
        var symbol = coinTradingState.getSymbol();
        var strategyState = flowState.getStrategies().get(CHAIN_STRATEGY);
        var priceOffset = strategyState.getPriceOffset();
        var marketState = marketRepository.getMarketState(symbol);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());

        var shortTriggerPrice = lastPrice
                .subtract(BigDecimal.valueOf(getPercentageOf(priceOffset, lastPrice.doubleValue(), lastPrice.scale())))
                .doubleValue();
        var shortOperationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, marketState, TradeOrderType.LIMIT, shortTriggerPrice);
 //       tradeOperationService.cancelAllOrders(credentials, CategoryType.LINEAR, symbol);
 //       tradeOperationService.makeLongCloseOperation(shortOperationContext);
        var shortFutureOperationContext = CompletableFuture.supplyAsync(() -> tradeOperationService.makeShortOperation(shortOperationContext));
        try {
            shortFutureOperationContext.get();
            return coinTradingState.forceStatus(flowType, SLEEPING);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private CoinIntervalTradingState processBuy(FlowState flowState, CoinIntervalTradingState coinTradingState, CredentialsState credentials) {

        var symbol = coinTradingState.getSymbol();
        var strategyState = flowState.getStrategies().get(CHAIN_STRATEGY);
        var priceOffset = strategyState.getPriceOffset();
        var marketState = marketRepository.getMarketState(symbol);
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());

        var longTriggerPrice = lastPrice
                .add(BigDecimal.valueOf(getPercentageOf(priceOffset, lastPrice.doubleValue(), lastPrice.scale())))
                .doubleValue();

        var longOperationContext = new TradeOperationService.OperationContext(coinTradingState, flowState, credentials, marketState, TradeOrderType.LIMIT, longTriggerPrice);
//        tradeOperationService.cancelAllOrders(credentials, CategoryType.LINEAR, symbol);
 //       tradeOperationService.makeShortCloseOperation(longOperationContext);
        var longFutureOrderContext = CompletableFuture.supplyAsync(() -> tradeOperationService.makeLongOperation(longOperationContext));
        try {
            longFutureOrderContext.get();
            return coinTradingState.forceStatus(flowType, SLEEPING);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
