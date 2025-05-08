package com.crypto.sick.trade.dto.enums;

public enum TradingStrategyStatusEnum {

    SLEEPING(1),
    PRE_BUY(2),
    BUY(3),
    PRE_SELL(2),
    SELL(3),
    STOP_LOSS(0),
    DISABLED(-1);

    int priority;

    TradingStrategyStatusEnum(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

}
