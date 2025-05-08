package com.crypto.sick.trade.config.external;

import com.crypto.sick.trade.dto.enums.PercentileEnum;
import com.crypto.sick.trade.dto.enums.TaapiIntervalEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "sick-trader.config")
@Data
public class AppConfig {

    List<PercentileEnum> supportedPercentile;
    List<TaapiIntervalEnum> supportedIntervals;
    boolean disableOnStopLoss;
    List<UserTradeConfig> users;
    boolean debugMode;
    boolean enabled = false;

}
