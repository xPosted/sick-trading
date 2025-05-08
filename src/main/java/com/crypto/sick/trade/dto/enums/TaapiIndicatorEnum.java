package com.crypto.sick.trade.dto.enums;

public enum TaapiIndicatorEnum {
    RSI("rsi"),
    MFI("mfi");

    private final String value;

    TaapiIndicatorEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
