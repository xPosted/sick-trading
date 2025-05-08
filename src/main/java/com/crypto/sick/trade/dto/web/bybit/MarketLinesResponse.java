package com.crypto.sick.trade.dto.web.bybit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketLinesResponse extends ByBitApiResponse{

    SymbolLines result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SymbolLines {

        String symbol;
        List<MarketLine> lines;

        @JsonCreator
        SymbolLines(@JsonProperty("symbol")String symbol, @JsonProperty("list") List<List<String>> list) {
            this.symbol = symbol;
            this.lines = list.stream().map(MarketLinesResponse::mapToMarketLine).toList();
        }

    }

    public static MarketLine mapToMarketLine(List<String> list) {
        MarketLine marketLine = new MarketLine();
        marketLine.setTimestamp(Long.parseLong(list.get(0)));
        marketLine.setOpen(Double.parseDouble(list.get(1)));
        marketLine.setHigh(Double.parseDouble(list.get(2)));
        marketLine.setLow(Double.parseDouble(list.get(3)));
        marketLine.setClose(Double.parseDouble(list.get(4)));
        marketLine.setVolume(Double.parseDouble(list.get(5)));
        return marketLine;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MarketLine {
        long timestamp;
        double open;
        double high;
        double low;
        double close;
        double volume;
    }

}
