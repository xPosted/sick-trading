package com.crypto.sick.trade.dto.enums;

public enum Symbol {
    BTCUSDT("BTCUSDT", 6, 3),
    LTCUSDT("LTCUSDT", 5, 1),
    ETHUSDT("ETHUSDT", 5, 2),
    SOLUSDT("SOLUSDT",2, 1),
    ADAUSDT("ADAUSDT", 2, 0),
    XRPUSDT("XRPUSDT", 2, 0),
    MNTUSDT("MNTUSDT", 2, 1),
    PEPEUSDT("PEPEUSDT", 0, 0),
    TONUSDT("TONUSDT",1, 1),
    DOGEUSDT("DOGEUSDT", 1, 0),
    NOTUSDT("NOTUSDT", 2,0),
    VIRTUALUSDT("VIRTUALUSDT", 0, 0),
    ELXUSDT("ELXUSDT", 0, 0),
    TRUMPUSDT("TRUMPUSDT", 1, 1),
    APEXUSDT("APEXUSDT",2, 1),
    HBARUSDT("HBARUSDT", 0, 0),
    BNBUSDT("BNBUSDT", 4, 2),
    TRXUSDT("TRXUSDT", 2, 0),
    DRIFTUSDT("DRIFTUSDT", 2, 0),
    ROAMUSDT("ROAMUSDT", 2,0),
    GALAUSDT("GALAUSDT", 2,0),
    SCAUSDT("SCAUSDT", 2, 1),
    GRTUSDT("GRTUSDT", 2,1),
    OBTUSDT("OBTUSDT", 1, 0),
    SHARPUSDT("SHARPUSDT",2, 0),
    SQDUSDT("SQDUSDT", 2,0),
    NSUSDT("NSUSDT", 0,0),
    DGBUSDT("DGBUSDT", 2,0),
    KAIAUSDT("KAIAUSDT", 0,0),
    SAFEUSDT("SAFEUSDT", 2,0),
    XDCUSDT("XDCUSDT", 1,0),
    SUIUSDT("SUIUSDT", 2,0),
    AVAXUSDT("AVAXUSDT", 2,1),
    AAVEUSDT("AAVEUSDT", 2,2),
    MKRUSDT("MKRUSDT", 3,3),
    TRBUSDT("TRBUSDT", 2,3);          // Not supported by TAAPI



    private final String value;
    private final int spotScale;
    private final int linearScale;

    Symbol(String value, int spotScale, int linearScale) {
        this.value = value;
        this.spotScale = spotScale;
        this.linearScale = linearScale;
    }

    public String getValue() {
        return value;
    }

    public int getSpotScale() {
        return spotScale;
    }

    public int getLinearScale() {
        return linearScale;
    }
}
