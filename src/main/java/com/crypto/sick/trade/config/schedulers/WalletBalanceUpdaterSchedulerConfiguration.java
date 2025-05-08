package com.crypto.sick.trade.config.schedulers;

import com.crypto.sick.trade.dto.enums.CoinEnum;
import com.crypto.sick.trade.data.user.UserStateEntity;
import com.crypto.sick.trade.service.UserService;
import com.crypto.sick.trade.service.WalletService;
import com.crypto.sick.trade.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.crypto.sick.trade.util.Utils.getAvailableWalletBalance;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Configuration
public class WalletBalanceUpdaterSchedulerConfiguration {

    @Autowired
    private UserService userService;
    @Autowired
    private WalletService walletService;

    @Async
    @Scheduled(fixedRateString = "${schedulers.walletUpdateInterval}", timeUnit = TimeUnit.SECONDS, initialDelay = 10)
    public void updateWalletBalance() {
        userService.findAll()
                .filter(UserStateEntity::isEnabled)
                .map(this::updateWalletBalance)
                .forEach(userService::save);
    }

    private UserStateEntity updateWalletBalance(UserStateEntity userStateEntity) {
        var credentials = userStateEntity.getCredentials();
        var walletResponse = walletService.getWallet(credentials);
        var balance = Arrays.stream(CoinEnum.values())
                .collect(toMap(identity(), coin -> getAvailableWalletBalance(coin, walletResponse)));
        return userStateEntity.withCoinBalances(balance)
                .syncWalletAndAcquiredQty();
    }

}
