package com.crypto.sick.trade.data.user;

import com.bybit.api.client.domain.trade.Side;
import com.crypto.sick.trade.dto.enums.FlowTypeEnum;
import com.crypto.sick.trade.dto.enums.OrderOperationTypeEnum;
import com.crypto.sick.trade.dto.web.bybit.OrderInfoEntry;
import com.crypto.sick.trade.dto.web.bybit.OrderInfoResult;
import com.crypto.sick.trade.util.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderContext {

    FlowTypeEnum flowType;
    OrderResultInfo orderResultInfo;
    OrderInfoResult orderInfoResult;
    double rsi;
    double mfi;
    double lastPrice;
    OrderOperationTypeEnum operationType;
    @With
    @Builder.Default
    boolean closed = false;

    @JsonIgnore
    public Double getAcquiredQty() {
        if (orderResultInfo.getRetCode() != 0 || orderInfoResult == null) {
            return 0.0;
        }
        var qty = orderInfoResult.getOrderEntries().stream()
                .findAny()
                .map(OrderInfoEntry::getOrderResultQty)
                .orElse(0.0);
        return orderResultInfo.getSide().equals(Side.BUY) ? qty : -qty;

    }

    @JsonIgnore
    public Side getSide() {
        return orderResultInfo.getSide();
    }

    @JsonIgnore
    public String getOrderId() {
        return orderResultInfo.getOrderResponse().getOrderId();
    }

    public static OrderContext buildOrderContext(FlowTypeEnum flowTypeEnum, OrderResultInfo orderResultInfo, OrderInfoResult infoResult, double rsi, double mfi, double lastPrice, OrderOperationTypeEnum operationType) {
        return OrderContext.builder()
                .flowType(flowTypeEnum)
                .orderResultInfo(orderResultInfo)
                .orderInfoResult(infoResult)
                .rsi(rsi)
                .mfi(mfi)
                .lastPrice(lastPrice)
                .operationType(operationType)
                .build();
    }

    @JsonIgnore
    public boolean isShortOperationType() {
        return operationType == OrderOperationTypeEnum.SHORT_OPERATION;
    }

    @JsonIgnore
    public boolean isLongOperationType() {
        return operationType == OrderOperationTypeEnum.LONG_OPERATION;
    }

    @JsonIgnore
    public boolean isOlderThanLast24Hours() {
        return System.currentTimeMillis() - orderResultInfo.getTime() > 24 * 60 * 60 * 1000;
    }

    @JsonIgnore
    public boolean isOlderThanLast6Hours() {
        return System.currentTimeMillis() - orderResultInfo.getTime() > 6 * 60 * 60 * 1000;
    }

    @JsonIgnore
    public boolean isOlderThanLast12Hours() {
        return System.currentTimeMillis() - orderResultInfo.getTime() > 12 * 60 * 60 * 1000;
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return orderResultInfo.getRetCode() == 0 && orderResultInfo.getRetMsg().equalsIgnoreCase("OK");
    }

}
