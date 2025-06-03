package com.crypto.sick.trade.config;

import feign.Client;
import feign.Feign;
import feign.micrometer.MicrometerCapability;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignMetricsConfig {

    private final MeterRegistry meterRegistry;

    public FeignMetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .client(new Client.Default(null, null))
                .addCapability(new MicrometerCapability(meterRegistry));
    }
}
