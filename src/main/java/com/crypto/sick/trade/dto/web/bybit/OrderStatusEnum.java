package com.crypto.sick.trade.dto.web.bybit;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatusEnum {
    CREATED("Created"),
    NEW("New"),
    REJECTED("Rejected"),
    PARTIALLY_FILLED("PartiallyFilled"),
    PARTIALLY_FILLED_CANCELED("PartiallyFilledCanceled"),
    FILLED("Filled"),
    CANCELLED("Cancelled"),
    UNTRIGGERED("Untriggered"),
    TRIGGERED("Triggered"),
    DEACTIVATED("Deactivated"),
    ACTIVE("Active");

    private final String description;

    private OrderStatusEnum(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return this.description;
    }

    public static OrderStatusEnum fromValue(String value) {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            if (status.description.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + value);
    }
}

