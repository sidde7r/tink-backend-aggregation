package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataDepositAssetEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssetDetailsResponse {
    private BankdataDepositAssetEntity depositAsset;
    private String holdingType;
    private int assetType;
    private String isinCode;
    private String securityId;
    private String name;
    private String currencyISO;
    private double stockPrice;
    private String stockPriceDate;
    private double buyPrice;
    private double sellPrice;
    private double currencyPrice;
    private double returns;
    private boolean buyAllowed;
    private boolean sellAllowed;
    private double deltaStockPricePct;
    private boolean showDeltaStockPricePct;
    private String risk;
    private double ratePoint;
    private double rateTopYear;
    private double rateLowYear;
    private String reutersSecurityId;
    private boolean userUsingRealtimeRates;
    private String stockExchangeRateTime;
    private double spotPriceAsk;
    private double spotPriceBid;
    private String stockExchangeName;
    private String spotPriceRateTime;
    private boolean usingReutersRates;

    public BankdataDepositAssetEntity getDepositAsset() {
        return depositAsset;
    }

    public String getHoldingType() {
        return holdingType;
    }

    public int getAssetType() {
        return assetType;
    }

    public String getIsinCode() {
        return isinCode;
    }

    public String getSecurityId() {
        return securityId;
    }

    public String getName() {
        return name;
    }

    public String getCurrencyISO() {
        return currencyISO;
    }

    public double getStockPrice() {
        return stockPrice;
    }

    public String getStockPriceDate() {
        return stockPriceDate;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public double getCurrencyPrice() {
        return currencyPrice;
    }

    public double getReturns() {
        return returns;
    }

    public boolean isBuyAllowed() {
        return buyAllowed;
    }

    public boolean isSellAllowed() {
        return sellAllowed;
    }

    public double getDeltaStockPricePct() {
        return deltaStockPricePct;
    }

    public boolean isShowDeltaStockPricePct() {
        return showDeltaStockPricePct;
    }

    public String getRisk() {
        return risk;
    }

    public double getRatePoint() {
        return ratePoint;
    }

    public double getRateTopYear() {
        return rateTopYear;
    }

    public double getRateLowYear() {
        return rateLowYear;
    }

    public String getReutersSecurityId() {
        return reutersSecurityId;
    }

    public boolean isUserUsingRealtimeRates() {
        return userUsingRealtimeRates;
    }

    public String getStockExchangeRateTime() {
        return stockExchangeRateTime;
    }

    public double getSpotPriceAsk() {
        return spotPriceAsk;
    }

    public double getSpotPriceBid() {
        return spotPriceBid;
    }

    public String getStockExchangeName() {
        return stockExchangeName;
    }

    public String getSpotPriceRateTime() {
        return spotPriceRateTime;
    }

    public boolean isUsingReutersRates() {
        return usingReutersRates;
    }
}
