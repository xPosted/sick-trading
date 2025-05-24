package com.crypto.sick.trade.service.strategy;

import com.bybit.api.client.domain.trade.Side;
import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.service.MarketRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Objects;

import static com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum.SLEEPING;
import static com.crypto.sick.trade.util.Utils.getPercentageOf;

@Slf4j
public class ChainStrategy implements TradingStrategy {

    private AppConfig appConfig;
    private MarketRepository marketRepository;
    @Getter
    private StrategyEnum strategyName;

    public ChainStrategy(AppConfig appConfig, MarketRepository marketRepository, StrategyEnum strategyName) {
        this.appConfig = appConfig;
        this.marketRepository = marketRepository;
        this.strategyName = strategyName;
    }

    @Override
    public StrategyEvaluationResult evaluate(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        var status = strategyState.getStatus();
        return switch (status) {
            case SLEEPING -> processSleeping(evaluationParams);
            case SETUP -> processInit(evaluationParams);
            case DISABLED ->
                    StrategyEvaluationResult.builder()
                            .strategy(strategyName)
                            .tradingStatus(TradingStrategyStatusEnum.DISABLED)
                            .build();

            default -> throw new RuntimeException("Unexpected state status: " + status);
        };


    }

    private StrategyEvaluationResult processSleeping(StrategyEvaluationParams evaluationParams) {
        var strategyState = evaluationParams.getStrategyState();
        var priceOffset = strategyState.getPriceOffset();
        var symbol = evaluationParams.getSymbol();
        var marketState = marketRepository.getMarketState(symbol);
        var lastStrategyPrice = strategyState.getLastPrice();
        var highCriticalValue = strategyState.getHighCriticalValue();
        var lowCriticalValue = strategyState.getLowCriticalValue();
        var marketPrice = marketState.getLastPrice();
        var status = strategyState.getStatus();

        if (Objects.isNull(lastStrategyPrice)) {
            return processInit(evaluationParams);
        }
        if (marketPrice > highCriticalValue) {
            var newHighCriticalValue = getNewHighCriticalValue(evaluationParams, Side.BUY);
            var newLowCriticalValue = getNewLowCriticalValue(evaluationParams, Side.BUY);
            return StrategyEvaluationResult.builder()
                    .strategy(strategyName)
                    .lastPrice(marketPrice)
                    .highCriticalValue(newHighCriticalValue)
                    .lowCriticalValue(newLowCriticalValue)
                    .tradingStatus(TradingStrategyStatusEnum.BUY)
                    .build();
        }

        if (marketPrice < lowCriticalValue) {
            var newHighCriticalValue = getNewHighCriticalValue(evaluationParams, Side.SELL);
            var newLowCriticalValue = getNewLowCriticalValue(evaluationParams, Side.SELL);
            return StrategyEvaluationResult.builder()
                    .strategy(strategyName)
                    .lastPrice(marketPrice)
                    .highCriticalValue(newHighCriticalValue)
                    .lowCriticalValue(newLowCriticalValue)
                    .tradingStatus(TradingStrategyStatusEnum.SELL)
                    .build();
        }

        return StrategyEvaluationResult.builder()
                .strategy(strategyName)
                .tradingStatus(SLEEPING)
                .build();
    }

    private StrategyEvaluationResult processInit(StrategyEvaluationParams evaluationParams) {
        var symbol = evaluationParams.getSymbol();
        var marketState = marketRepository.getMarketState(symbol);
        var highCriticalValue = getNewHighCriticalValue(evaluationParams, Side.SELL);
        var lowCriticalValue = getNewLowCriticalValue(evaluationParams, Side.SELL);


        var res = StrategyEvaluationResult.builder()
                .strategy(strategyName)
                .lastPrice(marketState.getLastPrice())
                .lowCriticalValue(lowCriticalValue)
                .highCriticalValue(highCriticalValue)
                .tradingStatus(TradingStrategyStatusEnum.SETUP)
                .build();
        log.info("{}: {}", symbol, res.toString());
        return res;
    }

    private Double getNewHighCriticalValue(StrategyEvaluationParams evaluationParams, Side side) {
        var strategyState = evaluationParams.getStrategyState();
        var priceOffset = strategyState.getPriceOffset();
        var symbol = evaluationParams.getSymbol();
        var marketState = marketRepository.getMarketState(symbol);
        var takeProfit = evaluationParams.getTakeProfit();
        var stopLoss = evaluationParams.getStopLoss();
        var lastPrice = BigDecimal.valueOf(marketState.getLastPrice());
        var step = switch (side) {
            case BUY -> takeProfit;
            case SELL -> stopLoss;
        };
        return lastPrice
                .add(BigDecimal.valueOf(getPercentageOf(step, lastPrice.doubleValue(), lastPrice.scale())))
                .add(BigDecimal.valueOf(getPercentageOf(priceOffset, lastPrice.doubleValue(), lastPrice.scale())))
                .doubleValue();
    }

    private Double getNewLowCriticalValue(StrategyEvaluationParams evaluationParams, Side side) {
        var strategyState = evaluationParams.getStrategyState();
        var priceOffset = strategyState.getPriceOffset();
        var symbol = evaluationParams.getSymbol();
        var marketState = marketRepository.getMarketState(symbol);
        var takeProfit = evaluationParams.getTakeProfit();
        var stopLoss = evaluationParams.getStopLoss();
        var lastPrice =BigDecimal.valueOf(marketState.getLastPrice());
        var step = switch (side) {
            case BUY -> stopLoss;
            case SELL -> takeProfit;
        };
       return lastPrice
                .subtract(BigDecimal.valueOf(getPercentageOf(step, lastPrice.doubleValue(), lastPrice.scale())))
                .subtract(BigDecimal.valueOf(getPercentageOf(priceOffset, lastPrice.doubleValue(), lastPrice.scale())))
                .doubleValue();
    }



}
