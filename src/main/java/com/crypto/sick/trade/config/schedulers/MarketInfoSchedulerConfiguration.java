package com.crypto.sick.trade.config.schedulers;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.state.MarketState;
import com.crypto.sick.trade.service.MarketInfoService;
import com.crypto.sick.trade.service.MarketRepository;
import com.crypto.sick.trade.service.TickerWebSocketService;
import com.crypto.sick.trade.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.bybit.api.client.domain.market.MarketInterval.*;

@Configuration
public class MarketInfoSchedulerConfiguration {

    private List<TickerWebSocketService> tickers;

    @Autowired
    private MarketInfoService marketInfoService;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private MarketRepository marketRepository;

    //   @Async
    //   @Scheduled(fixedRate = 1500)
    public void updateLastPriceHttp() {
        List<Symbol> symbols = Utils.getSymbols(appConfig);
        symbols.stream()
                .map(marketRepository::getMarketState)
                .forEach(this::updateLastPrice);
    }

    @PostConstruct
    public void init() {
        var symbols = splitSymbols(Utils.getSymbols(appConfig));
        tickers = symbols.stream()
                .map(part -> new TickerWebSocketService(marketRepository, part))
                .collect(Collectors.toList());
    }


    @Async
    @Scheduled(fixedRateString = "${schedulers.webSocketUpdateInterval}", timeUnit = TimeUnit.SECONDS, initialDelay = 7)
    public void updateLastPriceWebSocket() {
        tickers.forEach(TickerWebSocketService::tickerWebSocketConnectionUpdate);
    }

    private List<List<Symbol>> splitSymbols(List<Symbol> allSymbols) {
        int chunkSize = 5;
        int numChunks = (int) Math.ceil((double) allSymbols.size() / chunkSize);
        return allSymbols.stream()
                .collect(Collectors.groupingBy(s -> (allSymbols.indexOf(s) / chunkSize)))
                .values()
                .stream()
                .map(ArrayList::new)
                .collect(Collectors.toList());

    }

    //   @Scheduled(fixedRate = 60000)
    public void holdMarketLines() {
        List<Symbol> symbols = Utils.getSymbols(appConfig);

        symbols.stream()
                .map(marketRepository::getMarketState)
                .forEach(marketState -> updateMarketState(marketState, FIFTEEN_MINUTES, HALF_HOURLY));
    }

    private void updateMarketState(MarketState marketState, MarketInterval... marketIntervals) {
        Arrays.stream(marketIntervals)
                .forEach(interval -> marketState.update(marketInfoService.marketInfo(marketState.getSymbol(), interval)));
    }

    private void updateLastPrice(MarketState marketState) {
        var tickerResponse = marketInfoService.getTickers(CategoryType.SPOT, marketState.getSymbol());
        var lastPrice = Utils.getLastPrice(tickerResponse, marketState.getSymbol());
        marketState.setLastPrice(lastPrice);
    }

}
