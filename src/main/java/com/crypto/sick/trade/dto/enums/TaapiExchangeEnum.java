package com.crypto.sick.trade.dto.enums;

public enum TaapiExchangeEnum {

    BYBIT("bybit"),
    BINANCE("binance"),
    WHITEBIT("whitebit");

    private final String value;

    TaapiExchangeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
