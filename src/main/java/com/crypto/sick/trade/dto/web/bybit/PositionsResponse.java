package com.crypto.sick.trade.dto.web.bybit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class PositionsResponse extends ByBitApiResponse {

    PositionsCategoryWrapper result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class PositionsCategoryWrapper {
        private String category;
        @JsonProperty("list")
        private List<PositionDto> positions;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class PositionDto {
        private String symbol;
        private int leverage;
        private int autoAddMargin;
        private double avgPrice;
        private String liqPrice;
        private int riskLimitValue;
        private String takeProfit;
        private String positionValue;
        private boolean isReduceOnly;
        private String tpslMode;
        private int riskId;
        private double trailingStop;
        private String unrealisedPnl;
        private double markPrice;
        private int adlRankIndicator;
        private double cumRealisedPnl;
        private double positionMM;
        private long createdTime;
        private int positionIdx;
        private double positionIM;
        private long seq;
        private long updatedTime;
        private String side;
        private String bustPrice;
        private double positionBalance;
        private String leverageSysUpdatedTime;
        private double curRealisedPnl;
        private double size;
        private String positionStatus;
        private String mmrSysUpdatedTime;
        private String stopLoss;
        private int tradeMode;
        private String sessionAvgPrice;
    }

}
