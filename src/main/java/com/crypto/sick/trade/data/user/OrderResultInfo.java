package com.crypto.sick.trade.data.user;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.web.bybit.PlaceOrderResponse;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResultInfo {

    Symbol symbol;
    OrderResponse orderResponse;
    CategoryType categoryType;
    Side side;
    String qty;
    int retCode;
    String retMsg;
    long time;

    public static OrderResultInfo buildOrderResultInfo(PlaceOrderResponse orderResponse, Symbol symbol, Side side,
                                                        String qty) {
        return OrderResultInfo.builder()
                .symbol(symbol)
                .side(side)
                .qty(qty)
                .orderResponse(orderResponse.getResult())
                .time(orderResponse.getTime())
                .retCode(orderResponse.getRetCode())
                .retMsg(orderResponse.getRetMsg())
                .build();
    }

}
