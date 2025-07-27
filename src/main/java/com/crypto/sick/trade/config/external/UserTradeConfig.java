package com.crypto.sick.trade.config.external;

import com.crypto.sick.trade.data.user.UserStatusEnum;
import lombok.Data;

@Data
public class UserTradeConfig {

    String name;
    TradeConfig trade;
    UserStatusEnum status;

}
