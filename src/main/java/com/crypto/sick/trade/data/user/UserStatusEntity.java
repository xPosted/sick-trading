package com.crypto.sick.trade.data.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class UserStatusEntity {

    UserStatusEnum status;
    Long expirationTs;

    public static UserStatusEntity of(UserStatusEnum status, Long expirationTs) {
        return UserStatusEntity.builder()
                .status(status)
                .expirationTs(expirationTs)
                .build();
    }


    public static UserStatusEntity of(UserStatusEnum status) {
        return UserStatusEntity.builder()
                .status(status)
                .build();
    }

}
