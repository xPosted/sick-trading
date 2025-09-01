package com.crypto.sick.trade.dto.web.bybit.ws.orders;

import com.bybit.api.client.domain.CategoryType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class CategoryTypeDeserializer extends JsonDeserializer<CategoryType> {

    @Override
    public CategoryType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value != null) {
            return CategoryType.valueOf(value.toUpperCase());
        }
        return null;
    }
}
