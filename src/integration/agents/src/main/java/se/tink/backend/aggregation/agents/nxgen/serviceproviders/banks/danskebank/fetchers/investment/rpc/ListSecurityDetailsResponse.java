package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListSecurityDetailsResponse {
    private double tradeTotalVolume;
    private long tradeTotalCount;
    private double dayHigh;
    private double dayLow;
    private double yearHigh;
    private double yearLow;
    private double latestPrice;
    private String name;
    private String isin;
    private int type;
    private String securityTypeName;
    private String securitySubTypeName;
    private double maxBidAskPrice;
    private double latestPriceChange;
    private double latestPriceChangePct;
    private long delayInMinutes;
    private String latestTradePriceTime;
    private String currencyCode;
    private String latestPriceDate;
    private List<VolumePriceEntity> ask;
    private List<VolumePriceEntity> bid;
    private FundDetailsEntity fundDetails;
    private List<DividendEntity> dividend;
    private StockExchangeEntity stockExchange;
    private String currentTime;
    private int responseCode;

    public double getTradeTotalVolume() {
        return tradeTotalVolume;
    }

    public long getTradeTotalCount() {
        return tradeTotalCount;
    }

    public double getDayHigh() {
        return dayHigh;
    }

    public double getDayLow() {
        return dayLow;
    }

    public double getYearHigh() {
        return yearHigh;
    }

    public double getYearLow() {
        return yearLow;
    }

    public double getLatestPrice() {
        return latestPrice;
    }

    public String getName() {
        return name;
    }

    public String getIsin() {
        return isin;
    }

    public int getType() {
        return type;
    }

    public String getSecurityTypeName() {
        return securityTypeName;
    }

    public String getSecuritySubTypeName() {
        return securitySubTypeName;
    }

    public double getMaxBidAskPrice() {
        return maxBidAskPrice;
    }

    public double getLatestPriceChange() {
        return latestPriceChange;
    }

    public double getLatestPriceChangePct() {
        return latestPriceChangePct;
    }

    public long getDelayInMinutes() {
        return delayInMinutes;
    }

    public String getLatestTradePriceTime() {
        return latestTradePriceTime;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getLatestPriceDate() {
        return latestPriceDate;
    }

    public List<VolumePriceEntity> getAsk() {
        return ask;
    }

    public List<VolumePriceEntity> getBid() {
        return bid;
    }

    public FundDetailsEntity getFundDetails() {
        return fundDetails;
    }

    public List<DividendEntity> getDividend() {
        return dividend;
    }

    public StockExchangeEntity getStockExchange() {
        return stockExchange;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
