package com.crypto.sick.trade.config.schedulers;

import com.crypto.sick.trade.data.user.*;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.service.UserService;
import com.crypto.sick.trade.service.action.ActionRouter;
import com.crypto.sick.trade.service.strategy.StrategyEvaluationParams;
import com.crypto.sick.trade.service.strategy.StrategyEvaluationResult;
import com.crypto.sick.trade.service.strategy.TradingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.crypto.sick.trade.service.strategy.StrategyEvaluationParamsBuilder.buildStrategyEvaluationParams;

@Configuration
public class TradingSchedulerConfiguration {

    @Autowired
    private UserService userService;
    @Autowired
    private List<TradingStrategy> strategyList;
    @Autowired
    private ActionRouter actionRouter;

    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Scheduled(fixedRateString = "${schedulers.tradeInterval}", timeUnit = TimeUnit.SECONDS, initialDelay = 70)
    public void tradeOrDie() {
        cachedThreadPool.submit(() ->
                userService.getUserNames().entrySet().stream()
                        .parallel()
                        .forEach(this::tradeUserFunction)
        );
    }

    @PreDestroy
    public void shutdownThreadPool() {
        cachedThreadPool.shutdown();
    }

    private void tradeUserFunction(Map.Entry<String, ReentrantLock> entry) {
        var name = entry.getKey();
        var lock = entry.getValue();
        lock.lock();
        try {
            userService.updateTransactionally(name, this::tradeUser);
        } finally {
            lock.unlock();
        }
    }

    private UserStateEntity tradeUser(UserStateEntity userStateEntity) {
        if (!userStateEntity.isEnabled()) {
            return userStateEntity;
        }
        var credentials = userStateEntity.getCredentials();
        var updatedCategoryStates = userStateEntity.getCategoryTradingStates().values().stream()
                .map(categoryState -> tradeCategory(categoryState, credentials))
                .collect(Collectors.toMap(CategoryTradingState::getCategory, Function.identity()));
        return userStateEntity.toBuilder()
                .categoryTradingStates(updatedCategoryStates)
                .build();
    }

    private CategoryTradingState tradeCategory(CategoryTradingState categoryTradingState, CredentialsState credentials) {
        var updatedSymbolTradingStates =
                categoryTradingState.getCoinTradingStates().values().stream()
                        .map(coinTradingState -> CompletableFuture.supplyAsync(() -> tradeSymbol(coinTradingState, credentials), cachedThreadPool))
                        .map(CompletableFuture::join)
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
                .filter(s -> s.validStrategy(params))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found for evaluation: " + params.getStrategyState().getStrategy()));
        return strategy.evaluate(params);
    }

}
