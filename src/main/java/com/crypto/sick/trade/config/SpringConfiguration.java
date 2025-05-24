package com.crypto.sick.trade.config;

import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.service.MarketRepository;
import com.crypto.sick.trade.service.strategy.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@EnableScheduling
@EnableAsync
public class SpringConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public TradingStrategy rsiStrategy(AppConfig appConfig, MarketRepository marketRepository) {
        return new StochasticOscillatorStrategy(appConfig, marketRepository, StrategyEnum.RSI_STRATEGY);
    }

    @Bean
    public TradingStrategy mfiStrategy(AppConfig appConfig, MarketRepository marketRepository) {
        return new StochasticOscillatorStrategy(appConfig, marketRepository, StrategyEnum.MFI_STRATEGY);
    }

    @Bean
    public TradingStrategy hedgeStrategy(AppConfig appConfig, MarketRepository marketRepository) {
        return new HedgeStrategy(appConfig, marketRepository, StrategyEnum.HEDGE_STRATEGY);
    }

    @Bean
    public TradingStrategy chainStrategy(AppConfig appConfig, MarketRepository marketRepository) {
        return new ChainStrategy(appConfig, marketRepository, StrategyEnum.CHAIN_STRATEGY);
    }

    @Bean
    public OvertakeStrategy overtakeStrategy(MarketRepository marketRepository) {
        return new OvertakeStrategy(marketRepository);
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    @Bean
    FirebaseApp firebaseApp(GoogleCredentials credentials) {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    GoogleCredentials googleCredentials() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("sicktrading-firebase-adminsdk-credentials.json");
        return GoogleCredentials.fromStream(inputStream);
    }

}
