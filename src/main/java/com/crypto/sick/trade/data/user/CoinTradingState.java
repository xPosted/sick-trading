package com.crypto.sick.trade.data.user;

import com.bybit.api.client.domain.CategoryType;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TaapiIntervalEnum;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class  CoinTradingState {

    CategoryType category;
    Symbol symbol;
    Map<TaapiIntervalEnum, CoinIntervalTradingState> intervalStates;

    public CoinTradingState withUpdatedIntervalState(TaapiIntervalEnum interval, CoinIntervalTradingState updatedState) {
        var states = new HashMap<>(intervalStates);
        states.put(interval, updatedState);
        return this.toBuilder()
                .intervalStates(states)
                .build();
    }

    @JsonIgnore
    public CoinTradingState disable() {
       var disabledIntervals = intervalStates.values().stream()
               .map(CoinIntervalTradingState::disable)
               .collect(Collectors.toMap(CoinIntervalTradingState::getInterval, state -> state));
        return this.toBuilder()
                .intervalStates(disabledIntervals)
                .build();
    }

    public CoinTradingState syncWalletAndAcquiredQty(Double walletAvailable) {
        var states = new HashMap<>(intervalStates);
        BigDecimal walletAvailableBigDecimal = BigDecimal.valueOf(walletAvailable);
        var scaledAvailableQty = walletAvailableBigDecimal.setScale(symbol.getSpotScale(), RoundingMode.HALF_DOWN).doubleValue();
        states.forEach((interval, state) -> {
            if (state.getAcquiredQty() > scaledAvailableQty) {
                var updatedState = state.toBuilder()
                        .acquiredQty(scaledAvailableQty)
                        .build();
                states.put(interval, updatedState);
            }
        });
        return this.toBuilder()
                .intervalStates(states)
                .build();
    }

}
