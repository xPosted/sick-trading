package com.crypto.sick.trade.config.schedulers;

import com.bybit.api.client.domain.CategoryType;
import com.crypto.sick.trade.data.user.*;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.service.UserService;
import com.crypto.sick.trade.service.action.ActionRouter;
import com.crypto.sick.trade.service.strategy.StochasticOscillatorStrategy;
import com.crypto.sick.trade.service.strategy.StrategyEvaluationParams;
import com.crypto.sick.trade.service.strategy.StrategyEvaluationParamsBuilder;
import com.crypto.sick.trade.service.strategy.StrategyEvaluationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum.*;
import static com.crypto.sick.trade.service.strategy.StrategyEvaluationParamsBuilder.buildStrategyEvaluationParams;

@Configuration
public class TradingSchedulerConfiguration {

    @Autowired
    private UserService userService;
    @Autowired
    private List<StochasticOscillatorStrategy> strategyList;
    @Autowired
    private ActionRouter actionRouter;

    @Scheduled(fixedRateString = "${schedulers.tradeInterval}", timeUnit = TimeUnit.SECONDS, initialDelay = 70)
    public void tradeOrDie() {
        userService.findAll()
                .parallel()
                .filter(UserStateEntity::isEnabled)
                .map(this::tradeUser)
                .forEach(userService::save);
    }

    private UserStateEntity tradeUser(UserStateEntity userStateEntity) {
        var credentials = userStateEntity.getCredentials();
        var updatedCategoryStates = userStateEntity.getCategoryTradingStates().values().stream()
                .map(categoryState -> tradeCategory(categoryState, credentials))
                .collect(Collectors.toMap(CategoryTradingState::getCategory, Function.identity()));
        return userStateEntity.toBuilder()
                .categoryTradingStates(updatedCategoryStates)
                .build();
    }

    private CategoryTradingState tradeCategory(CategoryTradingState categoryTradingState, CredentialsState credentials) {
        var updatedSymbolTradingStates = categoryTradingState.getCoinTradingStates().values().stream()
                .map(coinTradingState -> tradeSymbol(coinTradingState, credentials))
                .collect(Collectors.toMap(CoinTradingState::getSymbol, coinTradingState -> coinTradingState));
        return categoryTradingState.toBuilder()
                .coinTradingStates(updatedSymbolTradingStates)
                .build();
    }


    private CoinTradingState tradeSymbol(CoinTradingState coinTradingState, CredentialsState credentials) {
        var updatedIntervalStates = coinTradingState.getIntervalStates().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> tradeInterval(entry.getValue(), credentials)));
        return coinTradingState.toBuilder()
                .intervalStates(updatedIntervalStates)
                .build();
    }

    private CoinIntervalTradingState tradeInterval(CoinIntervalTradingState coinTradingState, CredentialsState credentials) {
        return coinTradingState.getFlowStates().keySet().stream()
                .reduce(coinTradingState,
                        (state, flow) -> tradeFlow(state, flow, credentials),
                        (s1, s2) -> s2);
    }

    private CoinIntervalTradingState tradeFlow(CoinIntervalTradingState coinTradingState, FlowTypeEnum flow, CredentialsState credentials) {
        var flowState = coinTradingState.getFlowStates().get(flow);
        var evaluationParams = buildStrategyEvaluationParams(flow, coinTradingState);
        var updatedFlow = evaluationParams.stream()
                .map(this::evaluateStrategy)
                .reduce(flowState, FlowState::updateStatus, (s1, s2) -> s2);
        var updatedState = coinTradingState.withUpdatedFlowState(updatedFlow);
        return actionRouter.processAction(updatedState, credentials);
    }

    private StrategyEvaluationResult evaluateStrategy(StrategyEvaluationParams params) {
        var strategy = strategyList.stream()
                .filter(s -> s.getStrategyName().equals(params.getStrategyState().getStrategy()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found for evaluation: " + params.getStrategyState().getStrategy()));
        return strategy.evaluate(params);
    }

}
