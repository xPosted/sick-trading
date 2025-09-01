package com.crypto.sick.trade.dto.web.bybit.ws.orders;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.StopOrderType;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.web.bybit.OrderStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WsOrderResponseDto {

    private String topic;
    private String id;
    private long creationTime;
    private List<OrderData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderData {
        @JsonDeserialize(using = CategoryTypeDeserializer.class)
        private CategoryType category;
        private Symbol symbol;
        private String orderId;
        private String orderLinkId;
        private String blockTradeId;
        @JsonDeserialize(using = SideDeserializer.class)
        private Side side;
        private int positionIdx;
        private OrderStatusEnum orderStatus;
        private String cancelType;
        private String rejectReason;
        private String timeInForce;
        private String isLeverage;
        private String price;
        private String qty;
        private String avgPrice;
        private String leavesQty;
        private String leavesValue;
        private String cumExecQty;
        private String cumExecValue;
        private String cumExecFee;
        private String orderType;
        @JsonDeserialize(using = StopOrderTypeDeserializer.class)
        private StopOrderType stopOrderType;
        private String orderIv;
        private String triggerPrice;
        private String takeProfit;
        private String stopLoss;
        private String triggerBy;
        private String tpTriggerBy;
        private String slTriggerBy;
        private boolean reduceOnly;

    }

}
