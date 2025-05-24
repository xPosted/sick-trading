package com.crypto.sick.trade.service.strategy;

import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.service.MarketRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static com.crypto.sick.trade.util.Utils.getPercentageOf;
import static java.math.BigDecimal.valueOf;

@Slf4j
public class OvertakeStrategy implements TradingStrategy {

    private MarketRepository marketRepository;
    @Getter
    private StrategyEnum strategyName = StrategyEnum.OVERTAKE_STRATEGY;

    public OvertakeStrategy(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    @Override
    public StrategyEvaluationResult evaluate(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        var status = strategyState.getStatus();
        return switch (status) {
            case SLEEPING ->sleepingProcessor(evaluationParams);
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
        var priceOffset = getPercentageOf(strategyState.getPriceOffset(), price, valueOf(price).scale());

        return evaluationParams.getLastOrder()
                .filter(orderContext -> ! orderContext.isOlderThanLast12Hours())
                .filter(orderContext -> ! orderContext.isClosed())
                .map(orderContext -> {
                    switch (orderContext.getSide()) {
                        case BUY -> {
                            var lastOrderBuyPrice = orderContext.getLastPrice();
                            var lowCriticalValue = lastOrderBuyPrice - priceOffset;
                            if (price < lowCriticalValue && evaluationParams.getIsAvailableToBuy().apply(price)) {
                                return StrategyEvaluationResult.builder()
                                        .strategy(strategyName)
                                        .tradingStatus(TradingStrategyStatusEnum.BUY)
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
                            var highCriticalValue = lastOrderBuyPrice + priceOffset;
                            if (price > highCriticalValue && evaluationParams.getIsAvailableToSell().apply(price)) {
                                return StrategyEvaluationResult.builder()
                                        .strategy(strategyName)
                                        .tradingStatus(TradingStrategyStatusEnum.SELL)
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

}
