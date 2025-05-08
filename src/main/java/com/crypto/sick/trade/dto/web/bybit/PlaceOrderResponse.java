package com.crypto.sick.trade.dto.web.bybit;

import com.bybit.api.client.domain.trade.response.OrderResponse;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class PlaceOrderResponse extends ByBitApiResponse {

    OrderResponse result;

}
