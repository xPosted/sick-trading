package com.crypto.sick.trade.config.schedulers;

import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.dto.MarketLinesInfo;
import com.crypto.sick.trade.dto.enums.*;
import com.crypto.sick.trade.dto.state.*;
import com.crypto.sick.trade.dto.web.bybit.MarketLinesResponse;
import com.crypto.sick.trade.dto.web.taapi.TaapiIndicatorResult;
import com.crypto.sick.trade.service.MarketRepository;
import com.crypto.sick.trade.service.TaapiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.crypto.sick.trade.dto.enums.TaapiIntervalEnum.*;
import static com.crypto.sick.trade.util.Utils.*;

@Configuration
public class MarketAnalyticsSchedulerConfiguration {

    private static final Double RSI_LAZY_MAX = 64.0;
    private static final Double RSI_LAZY_MIN = 36.0;
    private static final Double MFI_LAZY_MAX = 71.0;
    private static final Double MFI_LAZY_MIN = 29.0;

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private TaapiService taapiService;

    //@Scheduled(fixedRate = 60000)
    public void calculateMarketPercentiles() {
        updatePercentiles();
    }

    @Async
    @Scheduled(fixedRateString = "${schedulers.rsiLazyUpdateInterval}", timeUnit = TimeUnit.SECONDS, initialDelay = 15)
    public void lazyUpdate() {
        optimisedUpdate(this::sleepingRangeFilter);
    }

    @Async
    @Scheduled(fixedRateString = "${schedulers.rsiForceUpdateInterval}", timeUnit = TimeUnit.SECONDS, initialDelay = 30)
    public void forceUpdate() {
        optimisedUpdate(state -> ! sleepingRangeFilter(state));
    }

    public void optimisedUpdate(Predicate<MarketState> filter) {
        var symbols = marketRepository.getMarketStates()
                .filter(filter)
                .map(MarketState::getSymbol)
                .toList();
        taapiService.getBulkIndicators(TaapiExchangeEnum.BYBIT, symbols, appConfig.getSupportedIntervals())
                .getResults()
                .entrySet()
                .forEach(this::updateMarketState);
    }

    private void updateMarketState(Map.Entry<Symbol, TaapiIndicatorResult> indicatorResultEntry) {
        var marketState = marketRepository.getMarketState(indicatorResultEntry.getKey());
        indicatorResultEntry.getValue().getRsiValues().forEach(marketState::updateRsi);
        indicatorResultEntry.getValue().getMfiValues().forEach(marketState::updateMfi);
    }

    private boolean sleepingRangeFilter(MarketState marketState) {
        return marketState.getRsi().values().stream()
                .allMatch(rsi -> rsi >= RSI_LAZY_MIN && rsi <= RSI_LAZY_MAX) ||
                marketState.getMfi().values().stream()
                        .allMatch(rsi -> rsi >= MFI_LAZY_MIN && rsi <= MFI_LAZY_MAX);
    }

    private void updatePercentiles() {
        List<Symbol> symbols = getSymbols(appConfig);
        symbols.stream()
                .map(symbol -> marketRepository.getMarketState(symbol))
                .forEach(this::updatePercentiles);

    }

    private void updatePercentiles(MarketState marketState) {
        marketState.getMarketLinesInfoMap().entrySet().stream()
                .forEach(entry -> {
                    marketState.getMarketAnalytics().updateMarketPercentile(entry.getKey(), buildMarketPercentile(entry.getValue()));
                });
    }

    private MarketPercentile buildMarketPercentile(MarketLinesInfo marketLinesInfo) {
        var result = MarketPercentile.empty();
        appConfig.getSupportedPercentile().stream()
                .forEach(percentile -> result.updatePercentile(percentile, calculatePercentile(marketLinesInfo, percentile)));
        return result;
    }

    private PercentileValues calculatePercentile(MarketLinesInfo marketLinesInfo, PercentileEnum percentile) {

        long startTime = marketLinesInfo.getMarketLinesResponseBody().getResult().getLines().stream()
                .map(MarketLinesResponse.MarketLine::getTimestamp)
                .min(Long::compareTo).orElse(0L);

        long endTime = marketLinesInfo.getMarketLinesResponseBody().getResult().getLines().stream()
                .map(MarketLinesResponse.MarketLine::getTimestamp)
                .max(Long::compareTo).orElse(0L);

        double minValue = marketLinesInfo.getMarketLinesResponseBody().getResult().getLines().stream()
                .map(MarketLinesResponse.MarketLine::getLow)
                .min(Double::compareTo).orElse(0.0);

        double maxValue = marketLinesInfo.getMarketLinesResponseBody().getResult().getLines().stream()
                .map(MarketLinesResponse.MarketLine::getHigh)
                .max(Double::compareTo).orElse(0.0);

        //    Stream<Double> lowValues = marketLinesInfo.getMarketLines().map(MarketLinesResponse.MarketLine::getLow);
        //    Stream<Double> highValues = marketLinesInfo.getMarketLines().map(MarketLinesResponse.MarketLine::getHigh);
        Stream<Double> lineAverage = marketLinesInfo.getMarketLines().map(marketLine -> (marketLine.getHigh() + marketLine.getLow()) / 2);

        List<Double> values = lineAverage
                .sorted(Double::compareTo)
                .toList();
        int percentileOffset = (ONE_HUNDRED - percentile.getValue()) / TWO;
        int marketOffset = values.size() / ONE_HUNDRED * percentileOffset;
        List<Double> percentileMarketValues = values.subList(marketOffset, values.size() - marketOffset);

        //    System.out.println("Min value: " + minValue + " Max value: " + maxValue);
        //    System.out.println("Start time: " + convertTimestampToDateTime(startTime) + " End time: " + convertTimestampToDateTime(endTime));
        //    System.out.println("70th percentile: " + percentileMarketValues.get(0) + " - " + percentileMarketValues.get(percentileMarketValues.size() - 1));
        return PercentileValues.builder()
                .startTime(startTime)
                .endTime(endTime)
                .low(percentileMarketValues.get(ZERO))
                .high(percentileMarketValues.get(percentileMarketValues.size() - ONE))
                .build();
    }

    private String convertTimestampToDateTime(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

}
