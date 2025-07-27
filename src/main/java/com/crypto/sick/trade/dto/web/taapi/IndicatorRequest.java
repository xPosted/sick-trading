package com.crypto.sick.trade.dto.web.taapi;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndicatorRequest {

    String id;
    String indicator;

}
