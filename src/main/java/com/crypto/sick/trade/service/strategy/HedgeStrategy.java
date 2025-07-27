package com.crypto.sick.trade.service.strategy;

import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.service.MarketRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.crypto.sick.trade.util.Utils.getPercentageOf;
import static java.math.BigDecimal.valueOf;

@Slf4j
public class HedgeStrategy implements TradingStrategy{

    private AppConfig appConfig;
    private MarketRepository marketRepository;
    @Getter
    private StrategyEnum strategyName;

    public HedgeStrategy(AppConfig appConfig, MarketRepository marketRepository, StrategyEnum strategyName) {
        this.appConfig = appConfig;
        this.marketRepository = marketRepository;
        this.strategyName = strategyName;
    }

    @Override
    public StrategyEvaluationResult evaluate(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        var status = strategyState.getStatus();
        return switch (status) {
            case SLEEPING ->sleepingProcessor(evaluationParams);
            case BUY, PRE_BUY -> preBuyProcessor(evaluationParams);
            case SELL, PRE_SELL -> preSellProcessor(evaluationParams);
            case DISABLED ->
                 StrategyEvaluationResult.builder()
                         .strategy(strategyName)
                        .tradingStatus(TradingStrategyStatusEnum.DISABLED)
                        .build();

            default -> throw new RuntimeException("Unexpected state status: " + status);
        };
    }

    private StrategyEvaluationResult sleepingProcessor(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        var symbol = evaluationParams.getSymbol();
        var marketState = marketRepository.getMarketState(symbol);
        var price = marketState.getLastPrice();
        var takeProfit = evaluationParams.getTakeProfit();
        var priceInitialOffset = getPercentageOf(strategyState.getPriceOffset(), price, valueOf(price).scale());
        var mainFlowLastOrder = evaluationParams.getLastOrders().stream()
                .filter(order -> order.getFlowType().equals(FlowTypeEnum.MAIN_FLOW))
                .findAny();
        var hedgeFlowLastOrder = evaluationParams.getLastOrders().stream()
                .filter(order -> order.getFlowType().equals(FlowTypeEnum.HEDGE_FLOW))
                .findAny();

        if (hedgeFlowLastOrder
                .filter(order -> ! order.isOlderThanLast3Hours())
                .isPresent())
        {
            return StrategyEvaluationResult.builder()
                    .strategy(strategyName)
                    .tradingStatus(TradingStrategyStatusEnum.SLEEPING)
                    .build();
        }
        return mainFlowLastOrder
                .filter(orderContext -> ! orderContext.isOlderThanLast3Hours())
                .map(orderContext -> {
                    switch (orderContext.getSide()) {
                        case BUY -> {
                            var lastOrderBuyPrice = orderContext.getLastPrice();
                            if (price < (lastOrderBuyPrice - priceInitialOffset)) {
                                var updatedLowValue = price - getPercentageOf(takeProfit, price, valueOf(price).scale());
                                return StrategyEvaluationResult.builder()
                                        .strategy(strategyName)
                                        .tradingStatus(TradingStrategyStatusEnum.SELL)
                                        .highCriticalValue(lastOrderBuyPrice)
                                        .lowCriticalValue(updatedLowValue)
                                        .timestamp(System.currentTimeMillis())
                                        .build();
                            }
                            return StrategyEvaluationResult.builder()
                                    .strategy(strategyName)
                                    .tradingStatus(TradingStrategyStatusEnum.SLEEPING)
                                    .build();
                        }
                        case SELL -> {
                            var lastOrderBuyPrice = orderContext.getLastPrice();
                            if (price > (lastOrderBuyPrice + priceInitialOffset)) {
                                var updatedHighValue = price + getPercentageOf(takeProfit, price, valueOf(price).scale());
                                return StrategyEvaluationResult.builder()
                                        .strategy(strategyName)
                                        .tradingStatus(TradingStrategyStatusEnum.BUY)
                                        .highCriticalValue(updatedHighValue)
                                        .lowCriticalValue(lastOrderBuyPrice)
                                        .timestamp(System.currentTimeMillis())
                                        .build();
                            }
                            return StrategyEvaluationResult.builder()
                                    .strategy(strategyName)
                                    .tradingStatus(TradingStrategyStatusEnum.SLEEPING)
                                    .build();
                        }
                        default -> throw new RuntimeException("Unexpected behavior in HedgeStrategy");
                    }
                })
                .orElseGet(() -> StrategyEvaluationResult.builder()
                        .strategy(strategyName)
                        .tradingStatus(TradingStrategyStatusEnum.SLEEPING)
                        .build());
    }

    private StrategyEvaluationResult preBuyProcessor(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        var symbol = evaluationParams.getSymbol();
        var marketState = marketRepository.getMarketState(symbol);
        var price = marketState.getLastPrice();
        var lowCriticalValue = strategyState.getLowCriticalValue();
        var highCriticalValue = strategyState.getHighCriticalValue();
        var takeProfit = evaluationParams.getTakeProfit();
        if (price < lowCriticalValue) {
            return StrategyEvaluationResult.builder()
                    .strategy(strategyName)
                    .tradingStatus(TradingStrategyStatusEnum.SLEEPING)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
        if (price > highCriticalValue) {
            var updatedHighValue = price + getPercentageOf(takeProfit, price, valueOf(price).scale());
            return StrategyEvaluationResult.builder()
                    .strategy(strategyName)
                    .tradingStatus(TradingStrategyStatusEnum.BUY)
                    .timestamp(System.currentTimeMillis())
                    .highCriticalValue(updatedHighValue)
                    .build();
        }
        return StrategyEvaluationResult.builder()
                .strategy(strategyName)
                .tradingStatus(TradingStrategyStatusEnum.PRE_BUY)
                .build();
    }

    private StrategyEvaluationResult preSellProcessor(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        var symbol = evaluationParams.getSymbol();
        var marketState = marketRepository.getMarketState(symbol);
        var price = marketState.getLastPrice();
        var lowCriticalValue = strategyState.getLowCriticalValue();
        var highCriticalValue = strategyState.getHighCriticalValue();
        var takeProfit = evaluationParams.getTakeProfit();
        if (price > highCriticalValue) {
            return StrategyEvaluationResult.builder()
                    .strategy(strategyName)
                    .tradingStatus(TradingStrategyStatusEnum.SLEEPING)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
        if (price < lowCriticalValue) {
            var updatedLowValue = price - getPercentageOf(takeProfit, price, valueOf(price).scale());
            return StrategyEvaluationResult.builder()
                    .strategy(strategyName)
                    .tradingStatus(TradingStrategyStatusEnum.SELL)
                    .timestamp(System.currentTimeMillis())
                    .lowCriticalValue(updatedLowValue)
                    .build();
        }
        return StrategyEvaluationResult.builder()
                .strategy(strategyName)
                .tradingStatus(TradingStrategyStatusEnum.PRE_SELL)
                .build();
    }


}
