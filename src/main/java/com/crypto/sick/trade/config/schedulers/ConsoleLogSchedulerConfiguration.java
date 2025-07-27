package com.crypto.sick.trade.config.schedulers;

import com.bybit.api.client.domain.market.MarketInterval;
import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.data.user.UserStateEntity;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.state.MarketPercentile;
import com.crypto.sick.trade.dto.state.MarketState;
import com.crypto.sick.trade.service.MarketRepository;
import com.crypto.sick.trade.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
public class ConsoleLogSchedulerConfiguration {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private UserService userService;

    @Async
    @Scheduled(fixedRate = 240000, initialDelay = 50000)
    public void printMarketAnalytics() {
        log.info("------------ User Orders ------------");

        userService.findAll()
                .filter(UserStateEntity::isEnabled)
                .forEach(userState -> {
                    log.info("User: {}", userState.getName());
                    userState.getCategoryTradingStates().forEach((category, categoryTradingState) -> {
                        log.info("\tCategory: {}", category);
                        categoryTradingState.getCoinTradingStates().forEach((symbol, coinTradingState) -> {
                            log.info("\t\tSymbol: {}", symbol);
                            coinTradingState.getIntervalStates().forEach((interval, state) -> {
                                log.info("\t\t\t {}", interval);
                                log.info("\t\t\t\t{}", state.getOrderHistory());
                            });
                        });
                    });
                });

        log.info("------------  ------------");
        log.info("------------ User Flow States ------------");

        userService.findAll().forEach(userState -> {
            log.info("User: {}", userState.getName());
            userState.getCategoryTradingStates().forEach((category, categoryTradingState) -> {
                log.info("\tCategory: {}", category);
                categoryTradingState.getCoinTradingStates().forEach((symbol, coinTradingState) -> {
                    log.info("\t\tSymbol: {}", symbol);
                    coinTradingState.getIntervalStates().forEach((interval, state) -> {
                        log.info("\t\t\t {}", interval);
                        state.getFlowStates().values().forEach(flowState -> {
                            log.info("\t\t\t\t{}", flowState);
                        });
                    });
                });
            });
        });

        log.info("------------  ------------");
        log.info("------------ Market Analytics ------------");
        log.info(String.valueOf(LocalDateTime.now()));
        marketRepository.getSymbols().stream()
                .map(Symbol::valueOf)
                .forEach(symbol -> {
                            log.info("Symbol: {}", symbol);
                            printMarketAnalytics(marketRepository.getMarketState(symbol));
                        }
                );
        log.info("------------  ------------\n");
    }

    private void printMarketAnalytics(MarketState marketState) {
        var marketAnalytics = marketState.getMarketAnalytics();
        log.info("Last price: {}", marketState.getLastPrice());
        log.info("RSI: {}", marketState.getRsi());
        log.info("MFI: {}", marketState.getMfi());
        // printPercentile(marketAnalytics.getMarketPercentiles());
    }

    private void printPercentile(Map<MarketInterval, MarketPercentile> marketPercentiles) {
        marketPercentiles
                .forEach((marketInterval, marketPercentile) -> {
                    log.info("\tMarket interval: {}:", marketInterval);
                    printPercentile(marketPercentile);
                });
    }

    private void printPercentile(MarketPercentile marketPercentile) {
        appConfig.getSupportedPercentile().forEach(percentile -> {
            var percentileValue = marketPercentile.getPercentile(percentile).get();
            log.info("\t\t{}: {} - {}", percentile, percentileValue.getLow(), percentileValue.getHigh());
        });
    }

}
