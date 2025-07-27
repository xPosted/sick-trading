package com.crypto.sick.trade.service;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.crypto.sick.trade.dto.MarketLinesInfo;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.web.bybit.MarketLinesResponse;
import com.crypto.sick.trade.dto.web.bybit.TickerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class MarketInfoService {

    private ObjectMapper objectMapper = new ObjectMapper();

    public MarketLinesInfo marketInfo(Symbol symbol, MarketInterval marketInterval) {
        var client = BybitApiClientFactory.newInstance().newMarketDataRestClient();
        var marketKLineRequest = buildKlineRequest(symbol, marketInterval);
        var response = client.getMarketLinesData(marketKLineRequest);
        var marketLinesResult = objectMapper.convertValue(response, MarketLinesResponse.class);
        return buildMarketLinesInfo(marketKLineRequest, marketLinesResult);
    }

    public TickerResponse getTickers(CategoryType category, Symbol symbol) {
        var client = BybitApiClientFactory.newInstance().newMarketDataRestClient();
        var tickerRequest = MarketDataRequest.builder().category(category).symbol(symbol.getValue()).build();
        var response = client.getMarketTickers(tickerRequest);
        return objectMapper.convertValue(response, TickerResponse.class);
    }

    private MarketDataRequest buildKlineRequest(Symbol symbol, MarketInterval marketInterval) {
        return MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol(symbol.getValue())
                .marketInterval(marketInterval)
                .limit(1000)
                .build();
    }

    private MarketLinesInfo buildMarketLinesInfo(MarketDataRequest marketDataRequest, MarketLinesResponse marketKlineResult) {
        return MarketLinesInfo.builder()
                .marketDataRequest(marketDataRequest)
                .marketLinesResponseBody(marketKlineResult)
                .build();
    }

}
