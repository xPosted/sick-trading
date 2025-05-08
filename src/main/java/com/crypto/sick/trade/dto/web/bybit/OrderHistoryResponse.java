package com.crypto.sick.trade.dto.web.bybit;

import com.bybit.api.client.domain.trade.response.OrderResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderHistoryResponse extends ByBitApiResponse {

    private OrderInfoResult result;

}
