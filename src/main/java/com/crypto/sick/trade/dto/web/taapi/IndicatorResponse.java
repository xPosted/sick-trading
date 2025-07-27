package com.crypto.sick.trade.dto.web.taapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndicatorResponse {

    String id;
    String indicator;
    IndicatorResponseResult result;

    @Data
    public static class IndicatorResponseResult {
        String value;
    }

    public String getValue() {
        return result.getValue();
    }
}

