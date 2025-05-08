package com.crypto.sick.trade.service;

import com.bybit.api.client.domain.CategoryType;
import com.crypto.sick.trade.config.external.*;
import com.crypto.sick.trade.data.user.*;
import com.crypto.sick.trade.dto.enums.*;
import com.crypto.sick.trade.util.Utils;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum.SLEEPING;

@Slf4j
@Service
public class OnStartValidation {

    @Autowired
    private WalletService walletService;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private UserService userService;
    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private Credentials credentials;

    @EventListener(ApplicationReadyEvent.class)
    public void validate() throws InterruptedException, FirebaseMessagingException {
        Thread.sleep(1000);
        disableAllUsers();
        validateConfig();
        // validateWallets();
        resetSettings();
        createEmptyMarketStates();
    }

    private void validateWallets() {
        if (!appConfig.isDebugMode()) {
            appConfig.getUsers().forEach(this::validateWalletBalance);
        }
    }

    private void createEmptyMarketStates() {
        List<Symbol> symbols = Utils.getSymbols(appConfig);
        symbols.forEach(marketRepository::getMarketState);
    }

    private void resetSettings() {
        appConfig.getUsers().forEach(this::resetUserSettings);
    }

    private void validateConfig() {
        var users = appConfig.getUsers();
        if (users == null || users.isEmpty()) {
            throw new RuntimeException("No user configuration found");
        }
        users.forEach(user -> {
            validateTradeConfigExists(user);
            validateCredentials(user.getName());
        });
    }

    private void validateTradeConfigExists(UserTradeConfig userTradeConfig) {
        if (userTradeConfig.getTrade() == null) {
            throw new RuntimeException("No trade configuration found for user " + userTradeConfig.getName());
        }
    }

    private CredentialsKeySecret validateCredentials(String userName) {
        var bybitUserCredentials = credentials.getBybitUserCredentials(userName);
        if (bybitUserCredentials.isEmpty()) {
            throw new RuntimeException("No credentials found for user " + userName);
        }
        return bybitUserCredentials.get();
    }

    private void disableAllUsers() {
        userService.findAll()
                .map(this::disableUser)
                .forEach(userService::save);
    }

    private UserStateEntity disableUser(UserStateEntity userStateEntity) {
        return userStateEntity.toBuilder()
                .enabled(false)
                .build();
    }


    private void resetUserSettings(UserTradeConfig userTradeConfig) {
        Optional.of(userService.getOrEmpty(userTradeConfig.getName()))
                .map(userStateEntity -> resetTradeSettings(userStateEntity, userTradeConfig))
                .map(this::resetCredentials)
        //        .ifPresent(user -> tradeOperationService.getOpenPositions(user.getCredentials(), CategoryType.LINEAR, Symbol.SCAUSDT, Side.SELL));  // TESTING ONLY
         .ifPresent(userService::save);
    }

    private UserStateEntity resetCredentials(UserStateEntity userStateEntity) {
        return userStateEntity.toBuilder()
                .credentials(CredentialsState.from(validateCredentials(userStateEntity.getName())))
                .build();
    }

    private void validateWalletBalance(UserTradeConfig userTradeConfig) {
        var bybitUserCredentials = credentials.getBybitUserCredentials(userTradeConfig.getName());
        if (bybitUserCredentials.isEmpty()) {
            throw new RuntimeException("No credentials found for user " + userTradeConfig.getName());
        }
        var walletResponse = walletService.getWallet(CredentialsState.from(bybitUserCredentials.get()));
        log.info("Wallet response: " + walletResponse);
        var usdtWalletResponse = walletResponse.getResult().getList().stream()
                .flatMap(acc -> acc.getCoin().stream())
                .filter(coin -> coin.getCoin().equals(CoinEnum.USDT.getCoin()))
                .findFirst()
                .orElseGet(() -> {
                    throw new RuntimeException("No wallet balance for coin USDT");
                });

        var totalUsdtAmountforTrading = userTradeConfig.getTrade().getSpot().values().stream()
                .flatMap(map -> map.values().stream())
                .map(SymbolIntervalTradeConfig::getBuyAmount)
                .reduce((a, b) -> a + b).orElse(0.0);
        if (totalUsdtAmountforTrading <= 0) {
            throw new RuntimeException("Total USDT amount for trading is less than or equal to zero");
        }
        if (totalUsdtAmountforTrading > (usdtWalletResponse.getWalletBalance() - usdtWalletResponse.getLocked())) {
            throw new RuntimeException("Total USDT amount for trading is more than wallet balance");
        }
    }

    private UserStateEntity resetTradeSettings(UserStateEntity stateEntity, UserTradeConfig userTradeConfig) {

        return stateEntity.toBuilder()
                .categoryTradingStates(buildCategoryTradingStates(stateEntity, userTradeConfig))
                .credentials(CredentialsState.from(validateCredentials(userTradeConfig.getName())))
                .enabled(userTradeConfig.isEnabled())
                .build();
    }

    private Map<CategoryType, CategoryTradingState> buildCategoryTradingStates(UserStateEntity stateEntity,
                                                                               UserTradeConfig userTradeConfig) {
        return Stream.of(CategoryType.SPOT, CategoryType.LINEAR)
           //     .filter(category -> ! userTradeConfig.getTrade().isEmpty(category))
                .map(category -> updateCategoryTradingState(category, stateEntity.getOrEmpty(category), userTradeConfig))
                .collect(Collectors.toMap(CategoryTradingState::getCategory, categoryTradingState -> categoryTradingState));
    }

    private CategoryTradingState updateCategoryTradingState(CategoryType category, CategoryTradingState existentCategoryTradingState, UserTradeConfig userTradeConfig) {
        return CategoryTradingState.builder()
                .category(category)
                .coinTradingStates(buildCoinTradingState(category, existentCategoryTradingState.getCoinTradingStates(), userTradeConfig.getTrade().get(category)))
                .build();

    }

    public Map<Symbol, CoinTradingState> buildCoinTradingState(CategoryType category, Map<Symbol, CoinTradingState> current, Map<Symbol, Map<TaapiIntervalEnum, SymbolIntervalTradeConfig>> symbolTradeConfig) {
        var result = current.values().stream()
                .map(CoinTradingState::disable)
                .collect(Collectors.toMap(CoinTradingState::getSymbol, coinTradingState -> coinTradingState));
        symbolTradeConfig.entrySet().stream()
                .map(entry -> CoinTradingState.builder()
                        .category(category)
                        .symbol(entry.getKey())
                        .intervalStates(buildCoinIntervalTradingState(category, entry.getKey(), getIntervalTradingStateOrEmpty(current, entry.getKey()), entry.getValue()))
                        .build())
                .forEach(symbolTradeState -> result.put(symbolTradeState.getSymbol(), symbolTradeState));
        return result;
    }

    private Map<TaapiIntervalEnum, CoinIntervalTradingState> getIntervalTradingStateOrEmpty(Map<Symbol, CoinTradingState> current, Symbol symbol) {
        return Optional.ofNullable(current.get(symbol))
                .map(CoinTradingState::getIntervalStates)
                .orElse(Map.of());
    }

    private Map<TaapiIntervalEnum, CoinIntervalTradingState> buildCoinIntervalTradingState(CategoryType category, Symbol symbol, Map<TaapiIntervalEnum, CoinIntervalTradingState> current, Map<TaapiIntervalEnum, SymbolIntervalTradeConfig> intervalStates) {
        return intervalStates.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> buildCoinIntervalTradingState(category, symbol, current.get(entry.getKey()), entry.getKey(), entry.getValue())));
    }

    private CoinIntervalTradingState buildCoinIntervalTradingState(CategoryType category, Symbol symbol, CoinIntervalTradingState current, TaapiIntervalEnum interval, SymbolIntervalTradeConfig symbolIntervalTradeConfig) {
        var builder = Optional.ofNullable(current).map(CoinIntervalTradingState::toBuilder).orElse(CoinIntervalTradingState.builder());
        var orderHistory = Optional.ofNullable(current).map(CoinIntervalTradingState::getOrderHistory).orElse(Set.of());
        return builder
                .category(category)
                .symbol(symbol)
                .interval(interval)
                .flowStates(buildFlowStates(symbolIntervalTradeConfig))
                .buyAmount(symbolIntervalTradeConfig.getBuyAmount())
                .sellAmount(symbolIntervalTradeConfig.getSellAmount())
                .leverage(symbolIntervalTradeConfig.getLeverage())
                .stopLoss(symbolIntervalTradeConfig.getStopLoss())
                .takeProfit(symbolIntervalTradeConfig.getTakeProfit())
                .orderHistory(orderHistory)
                .build();
    }

    private Map<FlowTypeEnum, FlowState> buildFlowStates(SymbolIntervalTradeConfig symbolIntervalTradeConfig) {
        return symbolIntervalTradeConfig.getFlows().entrySet().stream()
                .map(entry -> FlowState.builder()
                        .flowType(entry.getKey())
                        .status(SLEEPING)
                        .strategies(buildStrategiesStates(entry.getValue()))
                        .syncStrategies(entry.getValue().isSyncStrategies())
                        .build())
                .collect(Collectors.toMap(FlowState::getFlowType, flowState -> flowState));
    }

    public Map<StrategyEnum, StrategyState> buildStrategiesStates(FlowConfig flowConfig) {
        return flowConfig.getStrategies().entrySet().stream()
                .map(entry -> StrategyState.builder()
                        .strategy(entry.getKey())
                        .status(SLEEPING)
                        .statusTime(System.currentTimeMillis())
                        .priceOffset(entry.getValue().getPriceOffset())
                        .indicatorOffset(entry.getValue().getIndicatorOffset())
                        .stopLossTimeout(entry.getValue().getStopLossTimeout())
                        .highCriticalValue(entry.getValue().getHighCriticalValue())
                        .lowCriticalValue(entry.getValue().getLowCriticalValue())
                        .build())
                .collect(Collectors.toMap(StrategyState::getStrategy, strategyState -> strategyState));
    }

}
