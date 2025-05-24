package com.crypto.sick.trade.web;

import com.bybit.api.client.domain.CategoryType;
import com.crypto.sick.trade.data.user.OrderContext;
import com.crypto.sick.trade.data.user.StrategyState;
import com.crypto.sick.trade.data.user.UserStateEntity;
import com.crypto.sick.trade.dto.enums.*;
import com.crypto.sick.trade.dto.state.MarketState;
import com.crypto.sick.trade.dto.web.*;
import com.crypto.sick.trade.service.MarketRepository;
import com.crypto.sick.trade.service.TradeOperationService;
import com.crypto.sick.trade.service.UserService;
import com.crypto.sick.trade.service.action.ActionRouter;
import com.crypto.sick.trade.service.strategy.StrategyEvaluationResult;
import com.google.common.base.Functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.crypto.sick.trade.dto.enums.FlowTypeEnum.MAIN_FLOW;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private TradeOperationService tradeOperationService;
    @Autowired
    private ActionRouter actionRouter;

    @GetMapping(value = "/user/state", produces = "application/json")
    public UserStateInfo getUserState(@RequestParam String user) {
        var marketStates = marketRepository.getMarketStates().collect(Collectors.toList());
        return UserStateInfo.map(userService.findByName(user).orElseThrow(() -> new RuntimeException("User state is not available")), marketStates);
    }

    @GetMapping(value = "/user/orders", produces = "application/json")
    public OrderHistoryInfo getOrderHistory(@RequestParam String name, @RequestParam CategoryType category, @RequestParam Symbol symbol) {
        var userState = userService.findByName(name).orElseThrow(() -> new RuntimeException("User state is not available"));
        var categoryTradingState = userState.getCategoryTradingStates().get(category);
        var coinTradingState = categoryTradingState.getCoinTradingStates().get(symbol);
        return OrderHistoryInfo.buildOrderHistoryInfo(coinTradingState);
    }

    @PostMapping(value = "/user/config", produces = "application/json")
    public UserStateInfo updateTradingSettings(@RequestParam String name, @RequestParam Symbol symbol, @RequestParam CategoryType category, @RequestParam TaapiIntervalEnum interval, @RequestBody TradingSettingsInfo newSettings) {
        var updatedState = userService.findByName(name).map(state -> updateTradingSettings(state, symbol, category, interval, newSettings)).orElseThrow(() -> new RuntimeException("User state is not available"));
        userService.save(updatedState);
        var marketStates = marketRepository.getMarketStates().collect(Collectors.toList());
        return UserStateInfo.map(updatedState, marketStates);
    }

    @GetMapping(value = "/user/stat", produces = "application/json")
    public UserOrdersStatInfo getUserOrdersStat(@RequestParam String name) {
        var userState = userService.findByName(name).orElseThrow(() -> new RuntimeException("User state is not available"));
        return UserOrdersStatInfo.build(userState.getCategoryTradingStates().get(CategoryType.SPOT));
    }

    @PostMapping(value = "/user/status", produces = "application/json")
    public UserStateInfo getUserState(@RequestParam String user, @RequestParam boolean enabled) {
        var state = userService.findByName(user).orElseThrow(() -> new RuntimeException("User state is not available"));
        var updatedState = state.withEnabled(enabled);
        userService.save(updatedState);
        var marketStates = marketRepository.getMarketStates().collect(Collectors.toList());
        return UserStateInfo.map(updatedState, marketStates);
    }

    @GetMapping(value = "/users", produces = "application/json")
    public List<ShortUserInfo> getAllUsers() {
        return userService.findAll().map(ShortUserInfo::map).toList();
    }

    @DeleteMapping(value = "/user", produces = "application/json")
    public void deleteUser(@RequestParam String name) {
        userService.deleteByName(name);
    }

    @GetMapping(value = "/market", produces = "application/json")
    public List<MarketState> getMarketStates() {
        return marketRepository.getMarketStates().toList();
    }

    @PostMapping(value = "trade/action", produces = "application/json")
    public OrderContext makeAction(@RequestParam String user, @RequestParam CategoryType categoryType, @RequestParam Symbol symbol, @RequestParam TradingStrategyStatusEnum tradingStateType, @RequestParam FlowTypeEnum flow, @RequestParam TaapiIntervalEnum interval) {
        var userState = userService.findByName(user).orElseThrow(() -> new RuntimeException("User state is not available"));
        var categoryTradingState = userState.getCategoryTradingStates().get(categoryType);
        var coinTradingState = categoryTradingState.getCoinTradingStates().get(symbol);
        var coinIntervalTradingState = coinTradingState.getIntervalStates().get(interval);
        var updatedIntervalState = actionRouter.processAction(coinIntervalTradingState.updateStatus(flow, tradingStateType), userState.getCredentials());
        var updatedCoinState = coinTradingState.withUpdatedIntervalState(interval, updatedIntervalState);
        var updatedCategoryState = categoryTradingState.withUpdatedCoinState(symbol, updatedCoinState);
        var updatedUserState = userState.withUpdatedMarketState(updatedCategoryState);
        userService.save(updatedUserState);
        return updatedIntervalState
                .getLastSuccessfulOrderHistoryItem(flow)
                .orElseThrow();
    }

    private UserStateEntity updateTradingSettings(UserStateEntity userStateEntity, Symbol symbol, CategoryType category, TaapiIntervalEnum interval, TradingSettingsInfo newSettings) {
        var categoryTradingState = userStateEntity.getCategoryTradingStates().get(category);
        var coinTradingState = categoryTradingState.getCoinTradingStates().get(symbol);
        var coinIntervalTradingState = coinTradingState.getIntervalStates().get(interval);
        var updatedCoinIntervalTradingState = coinIntervalTradingState.toBuilder()
                .buyAmount(newSettings.getBuyAmount())
                .sellAmount(newSettings.getSellAmount())
                .stopLossTimeout(newSettings.getStopLossTimeout())
                .interval(interval)
                .build();
        var updatedCoinState = coinTradingState.withUpdatedIntervalState(interval, updatedCoinIntervalTradingState);
        var updatedCategoryState = categoryTradingState.withUpdatedCoinState(symbol, updatedCoinState);
        return userStateEntity.withUpdatedMarketState(updatedCategoryState);
    }

}
