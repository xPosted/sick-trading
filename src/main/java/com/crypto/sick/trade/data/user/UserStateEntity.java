package com.crypto.sick.trade.data.user;

import com.bybit.api.client.domain.CategoryType;
import com.crypto.sick.trade.data.converter.CoinBalancesConverter;
import com.crypto.sick.trade.data.converter.MarketTradingStatesConverter;
import com.crypto.sick.trade.dto.enums.CoinEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.util.Utils;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import javax.persistence.Convert;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.crypto.sick.trade.data.user.CategoryTradingState.buildEmptyCategoryTradingState;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class UserStateEntity {

    Long userId;
    String name;
    @With
    boolean enabled;
    CredentialsState credentials;

    @Builder.Default
    Map<CategoryType, CategoryTradingState> categoryTradingStates = new HashMap<>();


    @Builder.Default
    @With
    @Convert(converter = CoinBalancesConverter.class)
    Map<CoinEnum, Double> coinBalances = new HashMap<>();

    @DynamoDbPartitionKey
    public String getName() {
        return name;
    }

    @DynamoDbConvertedBy(MarketTradingStatesConverter.class)
    public Map<CategoryType, CategoryTradingState> getCategoryTradingStates() {
        return categoryTradingStates;
    }

    @DynamoDbConvertedBy(CoinBalancesConverter.class)
    public Map<CoinEnum, Double> getCoinBalances() {
        return coinBalances;
    }

    public UserStateEntity withUpdatedMarketState(CategoryTradingState categoryTradingState) {
        var updatedMarketStates = new HashMap<>(categoryTradingStates);
        updatedMarketStates.put(categoryTradingState.getCategory(), categoryTradingState);
        return this.toBuilder()
                .categoryTradingStates(updatedMarketStates)
                .build();
    }

    public CategoryTradingState getOrEmpty(CategoryType category) {
        return categoryTradingStates.getOrDefault(category, buildEmptyCategoryTradingState(category));
    }


    public Optional<Double> getCoinWallet(CoinEnum coin) {
        return Optional.ofNullable(coinBalances.get(coin));
    }

    public UserStateEntity syncWalletAndAcquiredQty() {
        return Optional.ofNullable(categoryTradingStates.get(CategoryType.SPOT))
                .map(this::syncWallet)
                .map(this::withUpdatedMarketState)
                .orElse(this);
    }

    private CategoryTradingState syncWallet(CategoryTradingState categoryTradingState) {
        var updatedCoinTradingStates = categoryTradingState.getCoinTradingStates().values().stream()
                .map(state -> state.syncWalletAndAcquiredQty(getAvailableInWallet(state.getSymbol())))
                .collect(Collectors.toMap(CoinTradingState::getSymbol, Function.identity()));
        return categoryTradingState.toBuilder()
                .coinTradingStates(updatedCoinTradingStates)
                .build();
    }

    private Double getAvailableInWallet(Symbol symbol) {
        return Optional.ofNullable(coinBalances.get(Utils.getCoin(symbol)))
                .orElse(0.0);
    }

}
