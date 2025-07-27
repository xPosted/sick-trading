package com.crypto.sick.trade.dto.web.bybit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletResponse extends ByBitApiResponse {

    private Result result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private List<Account> list;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {
        private double totalEquity;
        private double accountIMRate;
        private double totalMarginBalance;
        private double totalInitialMargin;
        private String accountType;
        private double totalAvailableBalance;
        private double accountMMRate;
        private double totalPerpUPL;
        private double totalWalletBalance;
        private double accountLTV;
        private double totalMaintenanceMargin;
        private List<Coin> coin;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coin {
        private String availableToBorrow;
        private double bonus;
        private double accruedInterest;
        private String availableToWithdraw;
        private double totalOrderIM;
        private double equity;
        private double totalPositionMM;
        private double usdValue;
        private double unrealisedPnl;
        private boolean collateralSwitch;
        private double spotHedgingQty;
        private String borrowAmount;
        private double totalPositionIM;
        private double walletBalance;
        private double cumRealisedPnl;
        private double locked;
        private boolean marginCollateral;
        private String coin;
    }

}