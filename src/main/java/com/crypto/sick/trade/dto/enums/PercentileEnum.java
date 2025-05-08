package com.crypto.sick.trade.dto.enums;

public enum PercentileEnum {
    PERCENTILE90(90),
    PERCENTILE85(85),
    PERCENTILE80(80),
    PERCENTILE75(75),
    PERCENTILE70(70),
    PERCENTILE65(65),
    PERCENTILE60(60),
    PERCENTILE55(55),
    PERCENTILE50(50);

    private int value;

    PercentileEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
