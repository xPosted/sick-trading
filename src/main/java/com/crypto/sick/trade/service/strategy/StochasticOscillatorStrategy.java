package com.crypto.sick.trade.service.strategy;

import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.service.MarketRepository;
import lombok.Getter;

import static com.crypto.sick.trade.util.Utils.calculateDiffPrcntAbs;
import static java.time.Instant.ofEpochMilli;
import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * RSI&MFI strategy implementation for trading.
 * <p>
 * This strategy evaluates the Stochastic Oscillator of a given market and makes trading decisions
 * based on predefined thresholds for buying and selling.
 */

public class StochasticOscillatorStrategy implements TradingStrategy {

    private AppConfig appConfig;
    private MarketRepository marketRepository;
    @Getter
    private StrategyEnum strategyName;

    public StochasticOscillatorStrategy(AppConfig appConfig, MarketRepository marketRepository, StrategyEnum strategyName) {
        this.appConfig = appConfig;
        this.marketRepository = marketRepository;
        this.strategyName = strategyName;
    }

    @Override
    public StrategyEvaluationResult evaluate(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        var status = strategyState.getStatus();
        switch (status) {
            case SLEEPING:
                return sleepingProcessor(evaluationParams);
            case BUY:
            case PRE_BUY:
                return preBuyProcessor(evaluationParams);
            case SELL:
            case PRE_SELL:
                return preSellProcessor(evaluationParams);
            case STOP_LOSS:
                return stopLossProcessor(evaluationParams);
            case DISABLED:
                return StrategyEvaluationResult.builder()
                        .strategy(strategyName)
                        .tradingStatus(TradingStrategyStatusEnum.DISABLED)
                        .build();
            default:
                throw new RuntimeException("Unexpected state status: " + status);
        }
    }

    private StrategyEvaluationResult sleepingProcessor(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        var symbol = evaluationParams.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        var interval = evaluationParams.getInterval();

        var lastPrice = targetMarketState.getLastPrice();
        var indicatorValue = targetMarketState.getIndicator(interval, strategyName);

        if (indicatorValue < strategyState.getLowCriticalValue()) {
            if (evaluationParams.getIsAvailableToBuy().apply(lastPrice)) {
                return StrategyEvaluationResult.builder()
                        .strategy(strategyName)
                        .lastPrice(lastPrice)
                        .tradingStatus(TradingStrategyStatusEnum.PRE_BUY)
                        .timestamp(System.currentTimeMillis())
                        .build();
            }
        }

        if (indicatorValue > strategyState.getHighCriticalValue()) {
            if (evaluationParams.getIsAvailableToSell().apply(lastPrice)) {
                return StrategyEvaluationResult.builder()
                        .strategy(strategyName)
                        .lastPrice(lastPrice)
                        .tradingStatus(TradingStrategyStatusEnum.PRE_SELL)
                        .timestamp(System.currentTimeMillis())
                        .build();
            }
        }
        return StrategyEvaluationResult.builder()
                .strategy(strategyName)
                .tradingStatus(TradingStrategyStatusEnum.SLEEPING)
                .build();
    }

    private StrategyEvaluationResult preBuyProcessor(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        var symbol = evaluationParams.getSymbol();
        var targetMarketState = marketRepository.getMarketState(symbol);
        var interval = evaluationParams.getInterval();
        var indicatorValue = targetMarketState.getIndicator(interval, strategyName);

        if (indicatorValue < strategyState.getLowCriticalValue() + strategyState.getIndicatorOffset()) {
            if (targetMarketState.getLastPrice() > strategyState.getLastPrice()) {
                if (calculateDiffPrcntAbs(strategyState.getLastPrice(), targetMarketState.getLastPrice()) > strategyState.getPriceOffset()) {
                    // make purchase
                    return StrategyEvaluationResult.builder()
                            .strategy(strategyName)
                            .tradingStatus(TradingStrategyStatusEnum.BUY)
                            .timestamp(System.currentTimeMillis())
                            .build();
                }
                return StrategyEvaluationResult.builder()
                        .strategy(strategyName)
                        .tradingStatus(TradingStrategyStatusEnum.PRE_BUY)
                        .build();
            }
            return StrategyEvaluationResult.builder()
                    .strategy(strategyName)
                    .tradingStatus(TradingStrategyStatusEnum.PRE_BUY)
                    .lastPrice(targetMarketState.getLastPrice())
                    .build();

        }
        return StrategyEvaluationResult.builder()
                .strategy(strategyName)
                .tradingStatus(TradingStrategyStatusEnum.SLEEPING)
                .lastPrice(strategyState.getLastPrice())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private StrategyEvaluationResult preSellProcessor(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        var symbol = evaluationParams.getSymbol();
        var interval = evaluationParams.getInterval();
        var targetMarketState = marketRepository.getMarketState(symbol);
        var indicatorValue = targetMarketState.getIndicator(interval, strategyName);

        if (indicatorValue > strategyState.getHighCriticalValue() - strategyState.getIndicatorOffset()) {
            if (targetMarketState.getLastPrice() < strategyState.getLastPrice()) {
                if (calculateDiffPrcntAbs(strategyState.getLastPrice(), targetMarketState.getLastPrice()) > strategyState.getPriceOffset()) {
                    // make sell
                    return StrategyEvaluationResult.builder()
                            .strategy(strategyName)
                            .tradingStatus(TradingStrategyStatusEnum.SELL)
                            .timestamp(System.currentTimeMillis())
                            .build();
                }
                return StrategyEvaluationResult.builder()
                        .strategy(strategyName)
                        .tradingStatus(TradingStrategyStatusEnum.PRE_SELL)
                        .build();
            }
            return StrategyEvaluationResult.builder()
                    .strategy(strategyName)
                    .tradingStatus(TradingStrategyStatusEnum.PRE_SELL)
                    .lastPrice(targetMarketState.getLastPrice())
                    .build();

        }
        return StrategyEvaluationResult.builder()
                .strategy(strategyName)
                .tradingStatus(TradingStrategyStatusEnum.SLEEPING)
                .lastPrice(strategyState.getLastPrice())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private StrategyEvaluationResult stopLossProcessor(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        if (ofEpochMilli(strategyState.getStatusTime()).plus(strategyState.getStopLossTimeout(), MINUTES)
                .isBefore(ofEpochMilli(System.currentTimeMillis()))) {
            return StrategyEvaluationResult.builder()
                    .strategy(strategyName)
                    .tradingStatus(TradingStrategyStatusEnum.SLEEPING)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
        return StrategyEvaluationResult.builder()
                .strategy(strategyName)
                .tradingStatus(TradingStrategyStatusEnum.STOP_LOSS)
                .timestamp(strategyState.getStatusTime())
                .build();
    }

}
