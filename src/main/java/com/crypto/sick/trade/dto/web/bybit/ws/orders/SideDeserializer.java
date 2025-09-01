package com.crypto.sick.trade.dto.web.bybit.ws.orders;

import com.bybit.api.client.domain.trade.Side;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class SideDeserializer extends JsonDeserializer<Side> {

    @Override
    public Side deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value != null) {
            return Side.valueOf(value.toUpperCase());
        }
        return null;
    }
}
