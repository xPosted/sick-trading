package com.crypto.sick.trade.dto.state;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class PercentileValues {

    long startTime;
    long endTime;
    double high;
    double low;

}
