package com.crypto.sick.trade.config.external;

import com.bybit.api.client.domain.CategoryType;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TaapiIntervalEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class TradeConfig {

    HashMap<Symbol, Map<TaapiIntervalEnum, SymbolIntervalTradeConfig>> spot;
    HashMap<Symbol, Map<TaapiIntervalEnum, SymbolIntervalTradeConfig>> linear;

    public HashMap<Symbol, Map<TaapiIntervalEnum, SymbolIntervalTradeConfig>> get(CategoryType category) {
        return switch (category) {
            case SPOT -> Optional.ofNullable(spot).orElse(new HashMap<>());
            case LINEAR -> Optional.ofNullable(linear).orElse(new HashMap<>());
            default -> throw new IllegalArgumentException("Unsupported category: " + category);
        };
    }

    public boolean isEmpty(CategoryType category) {
        return Optional.ofNullable(get(category))
                .map(HashMap::isEmpty)
                .orElse(true);
    }

}
