package com.crypto.sick.trade.config.schedulers;

import com.crypto.sick.trade.data.user.UserStateEntity;
import com.crypto.sick.trade.data.user.UserStatusEntity;
import com.crypto.sick.trade.data.user.UserStatusEnum;
import com.crypto.sick.trade.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class UserStatusUpdatesScheduler {

    @Autowired
    private UserService userService;

    @Async
    @Scheduled(fixedRateString = "${schedulers.statusVerifierInterval}", timeUnit = TimeUnit.SECONDS, initialDelay = 10)
    public void verifyStatuses() {
        userService.findAll()
                .map(this::verifyUserStatus)
                .forEach(userService::save);
    }

    private UserStateEntity verifyUserStatus(UserStateEntity userStateEntity) {
        var statusEntity = userStateEntity.getStatus();
        if (statusEntity.getExpirationTs() != null
                && statusEntity.getExpirationTs() < Instant.now().toEpochMilli()) {
            if (statusEntity.getStatus() == UserStatusEnum.DISABLED
                    || statusEntity.getStatus() == UserStatusEnum.STOP_LOSS) {
                log.info("User {} status expired: {}, new - {}", userStateEntity.getName(), statusEntity.getStatus(), UserStatusEnum.ENABLED);
                return userStateEntity.withStatus(UserStatusEntity.of(UserStatusEnum.ENABLED, null));
            }
            log.info("User {} status expired: {}, new - {}", userStateEntity.getName(), statusEntity.getStatus(), UserStatusEnum.DISABLED);
            return userStateEntity.withStatus(UserStatusEntity.of(UserStatusEnum.DISABLED, null));
        }
        return userStateEntity;
    }

}
