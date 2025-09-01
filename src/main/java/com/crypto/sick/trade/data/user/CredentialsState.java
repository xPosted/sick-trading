package com.crypto.sick.trade.data.user;

import com.crypto.sick.trade.config.external.CredentialsKeySecret;
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
public class CredentialsState {

    String key;
    String secret;
    String baseUrl;


    public static CredentialsState from(CredentialsKeySecret credentialsConfig) {
        return CredentialsState.builder()
                .key(credentialsConfig.getKey())
                .secret(credentialsConfig.getSecret())
                .baseUrl(credentialsConfig.getBaseUrl())
                .build();
    }

}
