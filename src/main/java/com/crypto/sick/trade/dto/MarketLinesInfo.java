package com.crypto.sick.trade.dto;

import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.crypto.sick.trade.dto.web.bybit.MarketLinesResponse;
import lombok.Builder;
import lombok.Value;

import java.util.stream.Stream;

@Value
@Builder
public class MarketLinesInfo {

    MarketDataRequest marketDataRequest;
    MarketLinesResponse marketLinesResponseBody;

    public Stream<MarketLinesResponse.MarketLine> getMarketLines() {
        return marketLinesResponseBody.getResult().getLines().stream();
    }

}
