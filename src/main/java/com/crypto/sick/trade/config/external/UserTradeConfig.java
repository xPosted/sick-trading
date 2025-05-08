package com.crypto.sick.trade.config.external;

import lombok.Data;

@Data
public class UserTradeConfig {

    String name;
    TradeConfig trade;
    boolean enabled;

}
