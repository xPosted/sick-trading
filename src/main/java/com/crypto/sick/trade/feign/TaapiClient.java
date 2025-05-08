package com.crypto.sick.trade.feign;

import com.crypto.sick.trade.dto.web.taapi.TaapiBulkRequest;
import com.crypto.sick.trade.dto.web.taapi.TaapiBulkResponse;
import com.crypto.sick.trade.dto.web.taapi.TaapiRsiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "taapi", url = "${sick-trader.config.credentials.taapiUrl}")
public interface TaapiClient {

    @GetMapping("/rsi")
    TaapiRsiResponse getMarketRSi(@RequestParam(name = "secret") String apiSecret,
                                  @RequestParam(name = "exchange") String exchange,
                                  @RequestParam(name = "symbol") String symbol,
                                  @RequestParam(name = "interval") String interval,
                                  @RequestParam(name = "backtrack") int backtrack);

    @PostMapping("/bulk")
    TaapiBulkResponse bulkGet(@RequestBody TaapiBulkRequest request);

}
