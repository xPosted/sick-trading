package com.crypto.sick.trade.dto.web.taapi;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TaapiBulkRequest {

    String secret;
    List<TaapiConstruct> construct;

}
