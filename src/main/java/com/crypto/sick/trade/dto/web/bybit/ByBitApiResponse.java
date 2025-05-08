package com.crypto.sick.trade.dto.web.bybit;

import lombok.Data;

@Data
public class ByBitApiResponse {

    int retCode;
    String retMsg;
    Object retExtInfo;
    Long time;

}
