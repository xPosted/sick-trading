package com.crypto.sick.trade.service;

import com.crypto.sick.trade.data.user.UserStateEntity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class UserService {

    private final DynamoDbTable<UserStateEntity> userTable;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Getter
    private Map<String, ReentrantLock> userNames;

    public UserService(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.userTable = dynamoDbEnhancedClient.table("sick.trading.users", TableSchema.fromBean(UserStateEntity.class));
        try {
            log.info("Creating table Users");
            this.userTable.createTable();
        } catch (ResourceInUseException e) {
            log.info("Table Users already exists");
        }

    }

    public void initUserNames() {
        userNames = findAll()
                .map(UserStateEntity::getName)
                .collect(Collectors.toUnmodifiableMap(Function.identity(), name -> new ReentrantLock(false)));
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

    /**
     * Updates a user document transactionally using the provided update function.
     * @param name the user name (partition key)
     * @param updateFunction function to update the UserStateEntity
     * @return the updated UserStateEntity
     */
    public UserStateEntity updateTransactionally(String name, java.util.function.UnaryOperator<UserStateEntity> updateFunction) {
        // Fetch current state
        UserStateEntity current = getOrEmpty(name);
        // Apply update
        UserStateEntity updated = updateFunction.apply(current);
        // Use DynamoDB transaction to save
        dynamoDbEnhancedClient.transactWriteItems(
            r -> r.addPutItem(userTable, updated)
        );
        return updated;
    }

}
