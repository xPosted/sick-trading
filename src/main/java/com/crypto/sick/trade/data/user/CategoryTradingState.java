package com.crypto.sick.trade.data.user;

import com.bybit.api.client.domain.CategoryType;
import com.crypto.sick.trade.dto.enums.Symbol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTradingState {

    CategoryType category;
    @Builder.Default
    Map<Symbol, CoinTradingState> coinTradingStates = new HashMap<>();

    public CategoryTradingState withUpdatedCoinState(Symbol symbol, CoinTradingState updatedCoinStates) {
        var states = new HashMap<>(coinTradingStates);
        states.put(symbol, updatedCoinStates);
        return this.toBuilder()
                .coinTradingStates(states)
                .build();
    }

    public static CategoryTradingState buildEmptyCategoryTradingState(CategoryType category) {
        return CategoryTradingState.builder()
                .category(category)
                .coinTradingStates(new HashMap<>())
                .build();
    }

}
