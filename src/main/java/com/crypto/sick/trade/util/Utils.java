package com.crypto.sick.trade.util;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.response.tickers.TickerEntry;
import com.bybit.api.client.domain.trade.PositionIdx;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.crypto.sick.trade.config.external.AppConfig;
import com.crypto.sick.trade.config.external.TradeConfig;
import com.crypto.sick.trade.config.external.UserTradeConfig;
import com.crypto.sick.trade.dto.enums.CoinEnum;
import com.crypto.sick.trade.dto.enums.StrategyEnum;
import com.crypto.sick.trade.dto.enums.Symbol;
import com.crypto.sick.trade.dto.enums.TradingStrategyStatusEnum;
import com.crypto.sick.trade.dto.web.bybit.PlaceOrderResponse;
import com.crypto.sick.trade.dto.web.bybit.TickerResponse;
import com.crypto.sick.trade.dto.web.bybit.WalletResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    public static final Integer ONE_HUNDRED = 100;
    public static final Integer ZERO = 0;
    public static final Integer ONE = 1;
    public static final Integer TWO = 2;
    public static final Integer FIVE = 5;
    public static final Double FIVE_POINT_THREE = 5.3D;

    public static final Set TAAPI_UNSUPPORTED_SYMBOLS = Set.of(
            Symbol.GUNUSDT, Symbol.XMRUSDT, Symbol.LAUNCHCOINUSDT, Symbol.ZORAUSDT, Symbol.DARKUSDT, Symbol.AWEUSDT,
            Symbol.BANKUSDT, Symbol.HAEDALUSDT, Symbol.SYRUPUSDT, Symbol.KAVAUSDT, Symbol.TRBUSDT
    );

    public static final Set WEBSOCKET_UNSUPPORTED_SYMBOLS = Set.of(
            Symbol.LAUNCHCOINUSDT, Symbol.ZORAUSDT, Symbol.DARKUSDT, Symbol.AWEUSDT, Symbol.BANKUSDT, Symbol.HAEDALUSDT, Symbol.SYRUPUSDT, Symbol.KAVAUSDT, Symbol.TRBUSDT
    );

    public static TradingStrategyStatusEnum getInitialStatus(StrategyEnum strategy) {
        return switch (strategy) {
            case CHAIN_STRATEGY -> TradingStrategyStatusEnum.SETUP;
            default -> TradingStrategyStatusEnum.SLEEPING;
        };
    }

    public static PositionIdx getPositionIds(Side side) {
        return switch (side) {
            case BUY -> PositionIdx.HEDGE_MODE_BUY;
            case SELL -> PositionIdx.HEDGE_MODE_SELL;
            default -> throw new IllegalArgumentException("Unsupported side: " + side);
        };
    }

    public static double getLastPrice(TickerResponse tickerResponse, Symbol symbol) {
        return tickerResponse.getResult().getTickerEntries().stream()
                .filter(ticker -> ticker.getSymbol().equals(symbol.getValue()))
                .findFirst()
                .map(TickerResponse.TickerEntry::getLastPrice)
                .map(Double::valueOf)
                .orElse(0.0);
    }

    public static List<Symbol> getSymbols(AppConfig appConfig) {
        return appConfig.getUsers().stream()
                .map(UserTradeConfig::getTrade)
                .flatMap(tradeConfig -> Stream.concat(Stream.of(tradeConfig.get(CategoryType.SPOT)), Stream.of(tradeConfig.get(CategoryType.LINEAR))))
                .map(Map::keySet)
                .flatMap(Set::stream)
                .toList();
    }

    public static PlaceOrderResponse buildPlaceOrderResponseStub() {
        var orderResponse = new OrderResponse();
        orderResponse.setOrderId("DEBUG-MODE");
        orderResponse.setOrderLinkId("DEBUG-MODE");
        var response = new PlaceOrderResponse(orderResponse);
        response.setTime(System.currentTimeMillis());
        return response;
    }

    public static Double getAvailableWalletBalance(CoinEnum targetCoin, WalletResponse walletResponse) {
        return walletResponse.getResult().getList().stream()
                .flatMap(acc -> acc.getCoin().stream())
                .filter(coin -> coin.getCoin().equals(targetCoin.getCoin()))
                .map(coin -> coin.getWalletBalance() - coin.getLocked())
                .findFirst()
                .orElse(0d);
    }

    public static CoinEnum getCoin(Symbol symbol) {
        switch (symbol) {
            case LTCUSDT -> {
                return CoinEnum.LTC;
            }
            case BTCUSDT -> {
                return CoinEnum.BTC;
            }
            case ETHUSDT -> {
                return CoinEnum.ETH;
            }
            case XRPUSDT -> {
                return CoinEnum.XRP;
            }
            case ADAUSDT -> {
                return CoinEnum.ADA;
            }
            case SOLUSDT -> {
                return CoinEnum.SOL;
            }
            case MNTUSDT -> {
                return CoinEnum.MNT;
            }
            case PEPEUSDT -> {
                return CoinEnum.PEPE;
            }
            case TONUSDT -> {
                return CoinEnum.TON;
            }
            case DOGEUSDT -> {
                return CoinEnum.DOGE;
            }
            case NOTUSDT -> {
                return CoinEnum.NOT;
            }
            case VIRTUALUSDT -> {
                return CoinEnum.VIRTUAL;
            }
            case ELXUSDT -> {
                return CoinEnum.ELX;
            }
            case TRUMPUSDT -> {
                return CoinEnum.TRUMP;
            }
            case APEXUSDT -> {
                return CoinEnum.APEX;
            }
            case HBARUSDT -> {
                return CoinEnum.HBAR;
            }
            case BNBUSDT -> {
                return CoinEnum.BNB;
            }
            case TRXUSDT -> {
                return CoinEnum.TRX;
            }
            case DRIFTUSDT -> {
                return CoinEnum.DRIFT;
            }
            case ROAMUSDT -> {
                return CoinEnum.ROAM;
            }
            case GALAUSDT -> {
                return CoinEnum.GALA;
            }
            case SCAUSDT -> {
                return CoinEnum.SCA;
            }
            case GRTUSDT -> {
                return CoinEnum.GRT;
            }
            case OBTUSDT -> {
                return CoinEnum.OBT;
            }
            case SHARPUSDT -> {
                return CoinEnum.SHARP;
            }
            case SQDUSDT -> {
                return CoinEnum.SQD;
            }
            case NSUSDT -> {
                return CoinEnum.NS;
            }
            case DGBUSDT -> {
                return CoinEnum.DGB;
            }
            case KAIAUSDT -> {
                return CoinEnum.KAIA;
            }
            case SAFEUSDT -> {
                return CoinEnum.SAFE;
            }
            case XDCUSDT -> {
                return CoinEnum.XDC;
            }
            case SUIUSDT -> {
                return CoinEnum.SUI;
            }
            case AVAXUSDT -> {
                return CoinEnum.AVAX;
            }
            case AAVEUSDT -> {
                    return CoinEnum.AAVE;
            }
            case MKRUSDT -> {
                return CoinEnum.MKR;
            }
            case TRBUSDT -> {
                return CoinEnum.TRB;
            }
            case CATIUSDT -> {
                return CoinEnum.CATI;
            }
            case GUNUSDT -> {
                return CoinEnum.GUN;
            }
            case XMRUSDT -> {
                return CoinEnum.XMR;
            }
            case COOKIEUSDT -> {
                return CoinEnum.COOKIE;
            }
            case KAVAUSDT -> {
                return CoinEnum.KAVA;
            }
            case LAUNCHCOINUSDT -> {
                return CoinEnum.LAUNCHCOIN;
            }
            case AWEUSDT -> {
                return CoinEnum.AWE;
            }
            case ZORAUSDT -> {
                return CoinEnum.ZORA;
            }
            case DARKUSDT -> {
                return CoinEnum.DARK;
            }
            default -> {
                throw new IllegalArgumentException("Unsupported symbol: " + symbol);
            }
        }
    }

    public static double calculateDiffPrcntAbs(double first, double second) {
        return Math.abs(calculateDiffPrcnt(first, second));
    }

    public static double calculateDiffPrcnt(double first, double second) {
        return (((second - first) / first) * ONE_HUNDRED);
    }

    public static Double getPercentageOf(double percent, double value, int scale) {
        var valueBigDecimal = BigDecimal.valueOf(value);
        var percentBigDecimal = BigDecimal.valueOf(percent);
        return valueBigDecimal
                .divide(BigDecimal.valueOf(ONE_HUNDRED), scale + 2, RoundingMode.DOWN)
                .multiply(percentBigDecimal)
                .setScale(scale, RoundingMode.DOWN)
                .doubleValue();
    }

    public static <T> List<List<T>> split(List<T> allSymbols, int groupSize) {
        int numChunks = (int) Math.ceil((double) allSymbols.size() / groupSize);
        return allSymbols.stream()
                .collect(Collectors.groupingBy(s -> (allSymbols.indexOf(s) / groupSize)))
                .values()
                .stream()
                .map(ArrayList::new)
                .collect(Collectors.toList());
    }

}
