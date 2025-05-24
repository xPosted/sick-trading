package com.crypto.sick.trade.dto.enums;

public enum TriggerDirectionEnum {

    RISES_TO(1),
    FALLS_TO(2);

    int value;

    TriggerDirectionEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
