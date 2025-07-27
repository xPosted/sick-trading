package com.crypto.sick.trade.dto.enums;

public enum TaapiSymbolEnum {
    BTCUSDT("BTC/USDT"),
    LTCUSDT("LTC/USDT"),
    ETHUSDT("ETH/USDT"),
    SOLUSDT("SOL/USDT"),
    ADAUSDT("ADA/USDT"),
    XRPUSDT("XRP/USDT"),
    MNTUSDT("MNT/USDT"),
    PEPEUSDT("PEPE/USDT"),
    TONUSDT("TON/USDT"),
    DOGEUSDT("DOGE/USDT"),
    NOTUSDT("NOT/USDT"),
    VIRTUALUSDT("VIRTUAL/USDT"),
    ELXUSDT("ELX/USDT"),
    TRUMPUSDT("TRUMP/USDT"),
    APEXUSDT("APEX/USDT"),
    HBARUSDT("HBAR/USDT"),
    BNBUSDT("BNB/USDT"),
    TRXUSDT("TRX/USDT"),
    DRIFTUSDT("DRIFT/USDT"),
    ROAMUSDT("ROAM/USDT"),
    GALAUSDT("GALA/USDT"),
    SCAUSDT("SCA/USDT"),
    GRTUSDT("GRT/USDT"),
    OBTUSDT("OBT/USDT"),
    SHARPUSDT("SHARP/USDT"),
    SQDUSDT("SQD/USDT"),
    NSUSDT("NS/USDT"),
    DGBUSDT("DGB/USDT"),
    KAIAUSDT("KAIA/USDT"),
    SAFEUSDT("SAFE/USDT"),
    XDCUSDT("XDC/USDT"),
    SUIUSDT("SUI/USDT"),
    AVAXUSDT("AVAX/USDT"),
    AAVEUSDT("AAVE/USDT"),
    MKRUSDT("MKR/USDT"),
    TRBUSDT("TRB/USDT"),
    CATIUSDT("CATI/USDT"),
    GUNUSDT("GUN/USDT"),
    XMRUSDT("XMR/USDT"),
    COOKIEUSDT("COOKIE/USDT"),
    KAVAUSDT("KAVA/USDT"),
    LAUNCHCOINUSDT("LAUNCHCOIN/USDT"),
    AWEUSDT("AWE/USDT"),
    ZORAUSDT("ZORA/USDT"),
    DARKUSDT("DARK/USDT"),
    BANKUSDT("BANK/USDT"),
    HAEDALUSDT("HAEDAL/USDT"),
    SYRUPUSDT("SYRUP/USDT"),
    UNIUSDT("UNI/USDT"),
    WIFUSDT("WIF/USDT"),
    ENAUSDT("ENA/USDT");


    private final String value;

    TaapiSymbolEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TaapiSymbolEnum from(Symbol symbol) {
        return switch (symbol) {
            case BTCUSDT -> TaapiSymbolEnum.BTCUSDT;
            case LTCUSDT -> TaapiSymbolEnum.LTCUSDT;
            case ETHUSDT -> TaapiSymbolEnum.ETHUSDT;
            case SOLUSDT -> TaapiSymbolEnum.SOLUSDT;
            case ADAUSDT -> TaapiSymbolEnum.ADAUSDT;
            case XRPUSDT -> TaapiSymbolEnum.XRPUSDT;
            case MNTUSDT -> TaapiSymbolEnum.MNTUSDT;
            case PEPEUSDT -> TaapiSymbolEnum.PEPEUSDT;
            case TONUSDT -> TaapiSymbolEnum.TONUSDT;
            case DOGEUSDT -> TaapiSymbolEnum.DOGEUSDT;
            case NOTUSDT -> TaapiSymbolEnum.NOTUSDT;
            case VIRTUALUSDT -> TaapiSymbolEnum.VIRTUALUSDT;
            case ELXUSDT -> TaapiSymbolEnum.ELXUSDT;
            case TRUMPUSDT -> TaapiSymbolEnum.TRUMPUSDT;
            case APEXUSDT -> TaapiSymbolEnum.APEXUSDT;
            case HBARUSDT -> TaapiSymbolEnum.HBARUSDT;
            case BNBUSDT -> TaapiSymbolEnum.BNBUSDT;
            case TRXUSDT -> TaapiSymbolEnum.TRXUSDT;
            case DRIFTUSDT -> TaapiSymbolEnum.DRIFTUSDT;
            case ROAMUSDT -> TaapiSymbolEnum.ROAMUSDT;
            case GALAUSDT -> TaapiSymbolEnum.GALAUSDT;
            case SCAUSDT -> TaapiSymbolEnum.SCAUSDT;
            case GRTUSDT -> TaapiSymbolEnum.GRTUSDT;
            case OBTUSDT -> TaapiSymbolEnum.OBTUSDT;
            case SHARPUSDT -> TaapiSymbolEnum.SHARPUSDT;
            case SQDUSDT -> TaapiSymbolEnum.SQDUSDT;
            case NSUSDT -> TaapiSymbolEnum.NSUSDT;
            case DGBUSDT -> TaapiSymbolEnum.DGBUSDT;
            case KAIAUSDT -> TaapiSymbolEnum.KAIAUSDT;
            case SAFEUSDT -> TaapiSymbolEnum.SAFEUSDT;
            case XDCUSDT -> TaapiSymbolEnum.XDCUSDT;
            case SUIUSDT -> TaapiSymbolEnum.SUIUSDT;
            case AVAXUSDT -> TaapiSymbolEnum.AVAXUSDT;
            case AAVEUSDT -> TaapiSymbolEnum.AAVEUSDT;
            case MKRUSDT -> TaapiSymbolEnum.MKRUSDT;
            case TRBUSDT -> TaapiSymbolEnum.TRBUSDT;
            case CATIUSDT -> TaapiSymbolEnum.CATIUSDT;
            case GUNUSDT -> TaapiSymbolEnum.GUNUSDT;
            case XMRUSDT -> TaapiSymbolEnum.XMRUSDT;
            case COOKIEUSDT -> TaapiSymbolEnum.COOKIEUSDT;
            case KAVAUSDT -> TaapiSymbolEnum.KAVAUSDT;
            case LAUNCHCOINUSDT -> TaapiSymbolEnum.LAUNCHCOINUSDT;
            case AWEUSDT -> TaapiSymbolEnum.AWEUSDT;
            case ZORAUSDT -> TaapiSymbolEnum.ZORAUSDT;
            case DARKUSDT -> TaapiSymbolEnum.DARKUSDT;
            case BANKUSDT -> TaapiSymbolEnum.BANKUSDT;
            case HAEDALUSDT -> TaapiSymbolEnum.HAEDALUSDT;
            case SYRUPUSDT -> TaapiSymbolEnum.SYRUPUSDT;
            case UNIUSDT -> TaapiSymbolEnum.UNIUSDT;
            case WIFUSDT -> TaapiSymbolEnum.WIFUSDT;
            case ENAUSDT -> TaapiSymbolEnum.ENAUSDT;

        };
    }

}
