package com.crypto.sick.trade.dto.web;

import com.bybit.api.client.domain.CategoryType;
import com.crypto.sick.trade.data.user.CategoryTradingState;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.state.MarketState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTradingInfo {

    CategoryType category;
    Map<Symbol, CoinTradingInfo> coinTradingStates;

    public static CategoryTradingInfo map(CategoryTradingState categoryTradingState, List<MarketState> marketStates) {
        var tradingInfo = categoryTradingState.getCoinTradingStates().values().stream()
                .map(tradingState -> CoinTradingInfo.map(tradingState, getLastPrice(tradingState.getSymbol(), marketStates)))
                .collect(Collectors.toMap(CoinTradingInfo::getSymbol, coinTradingInfo -> coinTradingInfo));
        return CategoryTradingInfo.builder()
                .category(categoryTradingState.getCategory())
                .coinTradingStates(tradingInfo)
                .build();
    }

    private static Double getLastPrice(Symbol symbol, List<MarketState> marketStates) {
        return marketStates.stream().filter(marketState -> marketState.getSymbol().equals(symbol))
                .map(MarketState::getLastPrice)
                .findFirst().orElse(0D);
    }

}
