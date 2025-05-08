package com.crypto.sick.trade.dto.web.taapi;

import com.crypto.sick.trade.dto.enums.TaapiSymbolEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TaapiConstruct {

    String exchange;
    TaapiSymbolEnum symbol;
    String interval;
    List<IndicatorRequest> indicators;

}
