package com.crypto.sick.trade.config.schedulers;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.data.user.UserStateEntity;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.state.MarketState;
import com.crypto.sick.trade.dto.web.bybit.ws.orders.WsOrderResponseDto;
import com.crypto.sick.trade.service.*;
import com.crypto.sick.trade.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.bybit.api.client.domain.market.MarketInterval.FIFTEEN_MINUTES;
import static com.bybit.api.client.domain.market.MarketInterval.HALF_HOURLY;

@Configuration
public class MarketInfoSchedulerConfiguration {

    private List<TickerWebSocketService> tickers;
    private List<OrdersWebSocketConnection> ordersWebSocketConnections = new ArrayList<>();
    private List<PositionsWebSocketConnection> positionWebSocketConnections = new ArrayList<>();

    @Autowired
    private UserService userService;
    @Autowired
    private MarketInfoService marketInfoService;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private StopLossService stopLossService;

    @Async
    @Scheduled(fixedRate = 1000, initialDelay = 7000)
    public void updateLastPriceHttp() {
        var allSymbols = Utils.getSymbols(appConfig).stream()
                .filter(Utils.WEBSOCKET_UNSUPPORTED_SYMBOLS::contains)
                .toList();
        allSymbols.stream()
                .parallel()
                .map(marketRepository::getMarketState)
                .forEach(this::updateLastPrice);
    }

    @PostConstruct
    public void init() {
        var allSymbols = Utils.getSymbols(appConfig).stream()
                .filter(s -> ! Utils.WEBSOCKET_UNSUPPORTED_SYMBOLS.contains(s))
                .collect(Collectors.toSet());
        var splitedSymbols = splitSymbols(new ArrayList<>(allSymbols));
        tickers = splitedSymbols.stream()
                .map(part -> new TickerWebSocketService(marketRepository, part))
                .collect(Collectors.toList());
    }

    private List<OrdersWebSocketConnection> initOrdersWebSocketConnections() {
        return userService.findAll()
                .filter(UserStateEntity::isEnabled)
                .map(state -> new OrdersWebSocketConnection(state.getName(), state.getCredentials(), closeOrderHandler(state.getName())))
                .toList();
    }

    private Consumer<List<WsOrderResponseDto.OrderData>> closeOrderHandler(String userName) {
        return orders -> orders.forEach(order -> stopLossService.onCloseOrderEvent(userName, order));
    }

    private List<PositionsWebSocketConnection> initPositionsWebSocketConnections() {
        return userService.findAll()
                .filter(UserStateEntity::isEnabled)
                .map(state -> new PositionsWebSocketConnection(state.getName(), state.getCredentials()))
                .toList();
    }


    @Async
    @Scheduled(fixedRateString = "${schedulers.webSocketUpdateInterval}", timeUnit = TimeUnit.SECONDS, initialDelay = 7)
    public void updateLastPriceWebSocket() {
        tickers.forEach(TickerWebSocketService::tickerWebSocketConnectionUpdate);
        updateOrderSocketConnection();
    }

    private void updateOrderSocketConnection() {
        ordersWebSocketConnections.forEach(OrdersWebSocketConnection::closeWebSocketConnection);
        ordersWebSocketConnections = initOrdersWebSocketConnections();
        ordersWebSocketConnections.forEach(OrdersWebSocketConnection::positionWebSocketConnectionUpdate);
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
        var tickerResponse = marketInfoService.getTickers(CategoryType.LINEAR, marketState.getSymbol());
        var lastPrice = Utils.getLastPrice(tickerResponse, marketState.getSymbol());
        marketState.setLastPrice(lastPrice);
    }

}
