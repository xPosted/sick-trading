package com.crypto.sick.trade.dto.web;

import com.bybit.api.client.domain.CategoryType;
import com.crypto.sick.trade.dto.enums.CoinEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.data.user.UserStateEntity;
import com.crypto.sick.trade.dto.state.MarketState;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class UserStateInfo {

    Long id;
    String name;
    boolean enabled;
    Map<CategoryType, CategoryTradingInfo> categoryTradingInfoMap;
    Map<CoinEnum, Double> coinBalances;

    public static UserStateInfo map(UserStateEntity userStateEntity, List<MarketState> marketStates) {

        var categoryInfos = userStateEntity.getCategoryTradingStates().values()
                .stream()
                .map(categoryTradingState -> CategoryTradingInfo.map(categoryTradingState, marketStates))
                .collect(Collectors.toMap(CategoryTradingInfo::getCategory, categoryTradingInfo -> categoryTradingInfo));
        return UserStateInfo.builder()
                .id(userStateEntity.getUserId())
                .name(userStateEntity.getName())
                .enabled(userStateEntity.isEnabled())
                .categoryTradingInfoMap(categoryInfos)
                .coinBalances(userStateEntity.getCoinBalances())
                .build();
    }



}
