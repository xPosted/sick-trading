package com.crypto.sick.trade.dto.web.bybit;

import com.bybit.api.client.domain.market.response.tickers.TickersResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class TickerResponse extends ByBitApiResponse {

    @JsonProperty("result")
    private TickerResult result;

    @Data
    public static class TickerResult {
        @JsonProperty("category")
        private String category;

        @JsonProperty("list")
        private List<TickerEntry> tickerEntries;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TickerEntry {
        @JsonProperty("symbol")
        private String symbol;

        @JsonProperty("lastPrice")
        private String lastPrice;

        @JsonProperty("indexPrice")
        private String indexPrice;

        @JsonProperty("markPrice")
        private String markPrice;

        @JsonProperty("prevPrice24h")
        private String prevPrice24h;

        @JsonProperty("price24hPcnt")
        private String price24hPcnt;

        @JsonProperty("highPrice24h")
        private String highPrice24h;

        @JsonProperty("lowPrice24h")
        private String lowPrice24h;

        @JsonProperty("prevPrice1h")
        private String prevPrice1h;

        @JsonProperty("openInterest")
        private String openInterest;

        @JsonProperty("openInterestValue")
        private String openInterestValue;

        @JsonProperty("turnover24h")
        private String turnover24h;

        @JsonProperty("volume24h")
        private String volume24h;

        @JsonProperty("fundingRate")
        private String fundingRate;

        @JsonProperty("nextFundingTime")
        private String nextFundingTime;

        @JsonProperty("predictedDeliveryPrice")
        private String predictedDeliveryPrice;

        @JsonProperty("basisRate")
        private String basisRate;

        @JsonProperty("deliveryFeeRate")
        private String deliveryFeeRate;

        @JsonProperty("deliveryTime")
        private String deliveryTime;

        @JsonProperty("ask1Size")
        private String ask1Size;

        @JsonProperty("bid1Price")
        private String bid1Price;

        @JsonProperty("ask1Price")
        private String ask1Price;

        @JsonProperty("bid1Size")
        private String bid1Size;

        @JsonProperty("basis")
        private String basis;
    }

}
