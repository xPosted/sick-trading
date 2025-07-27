package com.crypto.sick.trade.config.external;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Optional;

@Configuration
@ConfigurationProperties(prefix = "sick-trader.config.credentials")
@Data
public class Credentials {

    String taapiUrl;
    String taapiSecret;
    Map<String, CredentialsKeySecret> bybit;

    public Optional<CredentialsKeySecret> getBybitUserCredentials(String user) {
        return Optional.ofNullable(bybit.get(user));
    }

}
