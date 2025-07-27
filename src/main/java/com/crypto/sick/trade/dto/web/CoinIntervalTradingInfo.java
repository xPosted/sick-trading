package com.crypto.sick.trade.dto.web;

import com.crypto.sick.trade.data.user.CoinIntervalTradingState;
import com.crypto.sick.trade.dto.enums.StateStatus;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CoinIntervalTradingInfo {

    double acquiredQty;
    List<FlowInfo> flows;
    List<StateStatus> statuses;

    public static CoinIntervalTradingInfo map(CoinIntervalTradingState coinTradingState, double lastPrice) {
        var status = getStatus(coinTradingState, lastPrice);
        var flows = coinTradingState.getFlowStates().values().stream()
                .map(FlowInfo::from)
                .toList();
        return CoinIntervalTradingInfo.builder()
                .flows(flows)
                .acquiredQty(coinTradingState.getAcquiredQty())
                .statuses(status)
                .build();
    }

    private static List<StateStatus> getStatus(CoinIntervalTradingState coinTradingState, double lastPrice) {
        var statusResult = new ArrayList<StateStatus>();
        // TODO: remove hardcoded straightMode
        if (coinTradingState.isAvailableToBuy(lastPrice)) {
            statusResult.add(StateStatus.WAITING_FOR_BUY);
        }
        if (coinTradingState.isAvailableToSell(lastPrice)) {
             statusResult.add(StateStatus.WAITING_FOR_SELL);
        }
        return statusResult;
    }

}
