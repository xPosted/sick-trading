package com.crypto.sick.trade.data.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
@DynamoDbBean
public class CoinWalletEntity {

    double available;
    double tradingAmount;

    public CoinWalletEntity updateAvailable(double newAvailable) {
        if (newAvailable > tradingAmount) {
            return this.toBuilder()
                    .available(newAvailable)
                    .tradingAmount(newAvailable)
                    .build();
        }
        return this.toBuilder()
                .available(newAvailable)
                .build();
    }

    public static CoinWalletEntity empty() {
        return CoinWalletEntity.builder()
                .available(0)
                .tradingAmount(0)
                .build();
    }

}
