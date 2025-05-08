package com.crypto.sick.trade.dto.web.bybit;


import com.bybit.api.client.domain.TriggerBy;
import com.bybit.api.client.domain.trade.*;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder
@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class OrderInfoEntry {

    private String orderId;
    private String orderLinkId;
    private String blockTradeId;
    private String symbol;
    private String price;
    private String qty;
    private String side;
    private String isLeverage;
    private OrderStatusEnum orderStatus;
    private RejectReason rejectReason;
    private String avgPrice;
    private String leavesQty;
    private String leavesValue;
    private double cumExecQty;
    private String cumExecValue;
    private double cumExecFee;
    private TimeInForce timeInForce;
    private String orderType;
    private String takeProfit;
    private String stopLoss;
    private String tpslMode;
    private String tpLimitPrice;
    private String slLimitPrice;
    private String lastPriceOnCreated;
    private String createdTime;
    private String updatedTime;

    @JsonIgnore
    public Double getOrderResultQty() {
        var qty = cumExecQty -  cumExecFee;
        return BigDecimal.valueOf(qty)
                .setScale(7, RoundingMode.DOWN)
                .doubleValue();
    }

}
