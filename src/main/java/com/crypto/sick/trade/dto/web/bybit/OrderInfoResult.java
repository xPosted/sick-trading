package com.crypto.sick.trade.dto.web.bybit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder
@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class OrderInfoResult {

    @JsonProperty("category")
    private String category;
    @JsonProperty("list")
    private List<OrderInfoEntry> orderEntries;

}
