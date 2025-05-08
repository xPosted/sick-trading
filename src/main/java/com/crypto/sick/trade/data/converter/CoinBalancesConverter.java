package com.crypto.sick.trade.data.converter;

import com.crypto.sick.trade.data.user.CoinWalletEntity;
import com.crypto.sick.trade.dto.enums.CoinEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.persistence.Converter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CoinBalancesConverter implements AttributeConverter<Map<CoinEnum, CoinWalletEntity>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AttributeValue transformFrom(Map<CoinEnum, CoinWalletEntity> input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        try {
            return AttributeValue.builder().s(objectMapper.writeValueAsString(input)).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing map to JSON", e);
        }
    }

    @Override
    public Map<CoinEnum, CoinWalletEntity> transformTo(AttributeValue attributeValue) {
        if (Boolean.TRUE.equals(attributeValue.nul())) {
            return null;
        }
        try {
            return objectMapper.readValue(
                    attributeValue.s(),
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, CoinEnum.class, Double.class)
            );
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing JSON to map", e);
        }
    }

    @Override
    public EnhancedType<Map<CoinEnum, CoinWalletEntity>> type() {
        return EnhancedType.mapOf(EnhancedType.of(CoinEnum.class), EnhancedType.of(CoinWalletEntity.class));
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}