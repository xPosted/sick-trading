package com.crypto.sick.trade.dto.enums;

public enum TaapiIntervalEnum {

    INTERVAL_1M("1m"),
    INTERVAL_5m("5m"),
    INTERVAL_15m("15m"),
    INTERVAL_30m("30m"),
    INTERVAL_1h("1h"),
    INTERVAL_2h("2h"),
    INTERVAL_4h("4h"),
    INTERVAL_12h("12h"),
    INTERVAL_1d("1d"),
    INTERVAL_1w("1w");

    private final String value;

    TaapiIntervalEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
