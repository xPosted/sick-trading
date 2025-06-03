package com.crypto.sick.trade.service;

import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.config.external.Credentials;
import com.crypto.sick.trade.dto.enums.*;
import com.crypto.sick.trade.dto.web.taapi.*;
import com.crypto.sick.trade.feign.TaapiClient;
import com.crypto.sick.trade.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.crypto.sick.trade.dto.enums.TaapiIndicatorEnum.MFI;
import static com.crypto.sick.trade.dto.enums.TaapiIndicatorEnum.RSI;

@Service
public class TaapiService {

    private static final int CONSTRUCTS_LIMIT = 3;


    @Autowired
    private Credentials credentials;
    @Autowired
    private TaapiClient taapiClient;
    @Autowired
    private MeterRegistry meterRegistry;

    public Double getRsi(TaapiExchangeEnum exchange, Symbol symbol, TaapiIntervalEnum interval) {
        var taapiSymbol = TaapiSymbolEnum.from(symbol);
        var taapiRsi = taapiClient.getMarketRSi(credentials.getTaapiSecret(), exchange.getValue(), taapiSymbol.getValue(), interval.getValue(), 0);
        return taapiRsi.getValue();
    }

    public TaapiBulkResult getBulkIndicators(TaapiExchangeEnum exchange, List<Symbol> symbols, List<TaapiIntervalEnum> intervals) {
        var constructs = symbols.stream()
                .map(symbol -> buildConstructs(exchange, symbol, intervals))
                .flatMap(List::stream)
                .toList();
        var groupedBy = Utils.split(constructs, CONSTRUCTS_LIMIT);
        var result = TaapiBulkResult.builder().build();
        groupedBy.stream()
                .map(this::makeRequest)
                .map(TaapiBulkResponse::getData)
                .flatMap(List::stream)
                .forEach(result::addIndicatorResult);
        return result;
    }

    private TaapiBulkResponse makeRequest(List<TaapiConstruct> constructs) {
        var requestBody = TaapiBulkRequest.builder()
                .secret(credentials.getTaapiSecret())
                .construct(constructs)
                .build();
        return taapiClient.bulkGet(requestBody);
    }

    private List<TaapiConstruct> buildConstructs(TaapiExchangeEnum exchange, Symbol symbol, List<TaapiIntervalEnum> intervals) {
        return intervals.stream()
                .map(interval -> TaapiConstruct.builder()
                        .interval(interval.getValue())
                        .exchange(exchange.getValue())
                        .symbol(TaapiSymbolEnum.from(symbol))
                        .indicators(List.of(buildIndicatorRequest(symbol, interval, RSI), buildIndicatorRequest(symbol, interval, MFI)))
                        .build())
                .toList();
    }

    public IndicatorRequest buildIndicatorRequest(Symbol symbol, TaapiIntervalEnum interval, TaapiIndicatorEnum indicator) {
        return IndicatorRequest.builder()
                .id(buildId(symbol, interval, indicator))
                .indicator(indicator.getValue())
                .build();
    }

    private String buildId(Symbol symbol, TaapiIntervalEnum interval, TaapiIndicatorEnum indicator) {
        return symbol.name() +":"+interval.name()+":"+indicator.name();
    }

}
