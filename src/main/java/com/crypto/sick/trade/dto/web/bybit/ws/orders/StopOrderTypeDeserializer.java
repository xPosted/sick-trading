package com.crypto.sick.trade.dto.web.bybit.ws.orders;

import com.bybit.api.client.domain.trade.StopOrderType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Arrays;

public class StopOrderTypeDeserializer extends JsonDeserializer<StopOrderType> {

    @Override
    public StopOrderType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value != null) {
            return Arrays.stream(StopOrderType.values())
                    .filter(type -> type.getDescription().equalsIgnoreCase(value))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
