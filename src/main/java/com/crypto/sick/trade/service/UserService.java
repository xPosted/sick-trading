package com.crypto.sick.trade.service;

import com.crypto.sick.trade.data.user.UserStateEntity;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class UserService {

    private final DynamoDbTable<UserStateEntity> userTable;

    public UserService(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.userTable = dynamoDbEnhancedClient.table("sick.trading.users", TableSchema.fromBean(UserStateEntity.class));
        try {
            log.info("Creating table Users");
            this.userTable.createTable();
        } catch (ResourceInUseException e) {
            log.info("Table Users already exists");
        }

    }

    public UserStateEntity getOrEmpty(String name) {
        return findByName(name)
                .orElse(UserStateEntity.builder()
                        .name(name)
                        .build()
                );
    }


    public Optional<UserStateEntity> findByName(String name) {
        return Optional.ofNullable(userTable.getItem(r -> r.key(k -> k.partitionValue(name))));
    }

    public void save(UserStateEntity userStateEntity) {
        userTable.putItem(userStateEntity);
    }

    public void deleteByName(String name) {
        userTable.deleteItem(r -> r.key(k -> k.partitionValue(name)));
    }

    public Stream<UserStateEntity> findAll() {
        return userTable.scan().items().stream();
    }

}
