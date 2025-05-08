package com.crypto.sick.trade.data.converter;

import com.bybit.api.client.domain.CategoryType;
import com.crypto.sick.trade.data.user.CoinTradingState;
import com.crypto.sick.trade.data.user.CategoryTradingState;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MarketTradingStatesConverter implements AttributeConverter<Map<CategoryType, CategoryTradingState>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AttributeValue transformFrom(Map<CategoryType, CategoryTradingState> input) {
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
    public Map<CategoryType, CategoryTradingState> transformTo(AttributeValue attributeValue) {
        if (Boolean.TRUE.equals(attributeValue.nul())) {
            return null;
        }
        try {
            return objectMapper.readValue(
                    attributeValue.s(),
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, CategoryType.class, CategoryTradingState.class)
            );
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing JSON to map", e);
        }
    }

    @Override
    public EnhancedType<Map<CategoryType, CategoryTradingState>> type() {
        return EnhancedType.mapOf(EnhancedType.of(CategoryType.class), EnhancedType.of(CategoryTradingState.class));
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}