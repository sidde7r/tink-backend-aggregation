package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.log.AggregationLogger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StockEntity {
    @JsonIgnore
    private static final AggregationLogger log = new AggregationLogger(StockEntity.class);

    private boolean allowTrade;
    private String buyRate;
    private String closingRate;
    private String currency;
    private String earningsPerShare;
    private String id;
    private String localizedRate;
    private String name;
    private String peValue;
    private String performanceDirection;
    private String performanceToday;
    private int performanceTodayInCents;
    private String performanceTodayInCurrency;
    private double rate;
    private String rateTimestamp;
    private String sellRate;
    private String volatility;
    private String yield;

    @JsonIgnore
    private String isin;
    @JsonIgnore
    private String market;

    public boolean isAllowTrade() {
        return allowTrade;
    }

    public void setAllowTrade(boolean allowTrade) {
        this.allowTrade = allowTrade;
    }

    public String getBuyRate() {
        return buyRate;
    }

    public void setBuyRate(String buyRate) {
        this.buyRate = buyRate;
    }

    public String getClosingRate() {
        return closingRate;
    }

    public void setClosingRate(String closingRate) {
        this.closingRate = closingRate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getEarningsPerShare() {
        return earningsPerShare;
    }

    public void setEarningsPerShare(String earningsPerShare) {
        this.earningsPerShare = earningsPerShare;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocalizedRate() {
        return localizedRate;
    }

    public void setLocalizedRate(String localizedRate) {
        this.localizedRate = localizedRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPeValue() {
        return peValue;
    }

    public void setPeValue(String peValue) {
        this.peValue = peValue;
    }

    public String getPerformanceDirection() {
        return performanceDirection;
    }

    public void setPerformanceDirection(String performanceDirection) {
        this.performanceDirection = performanceDirection;
    }

    public String getPerformanceToday() {
        return performanceToday;
    }

    public void setPerformanceToday(String performanceToday) {
        this.performanceToday = performanceToday;
    }

    public int getPerformanceTodayInCents() {
        return performanceTodayInCents;
    }

    public void setPerformanceTodayInCents(int performanceTodayInCents) {
        this.performanceTodayInCents = performanceTodayInCents;
    }

    public String getPerformanceTodayInCurrency() {
        return performanceTodayInCurrency;
    }

    public void setPerformanceTodayInCurrency(String performanceTodayInCurrency) {
        this.performanceTodayInCurrency = performanceTodayInCurrency;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getRateTimestamp() {
        return rateTimestamp;
    }

    public void setRateTimestamp(String rateTimestamp) {
        this.rateTimestamp = rateTimestamp;
    }

    public String getSellRate() {
        return sellRate;
    }

    public void setSellRate(String sellRate) {
        this.sellRate = sellRate;
    }

    public String getVolatility() {
        return volatility;
    }

    public void setVolatility(String volatility) {
        this.volatility = volatility;
    }

    public String getYield() {
        return yield;
    }

    public void setYield(String yield) {
        this.yield = yield;
    }

    public String getIsin() {
        if (isin == null || isin.isEmpty()) {
            parseIsinAndMarket();
        }

        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getMarket() {
        if (market == null || market.isEmpty()) {
            parseIsinAndMarket();
        }

        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    private void parseIsinAndMarket() {
        if (getId() == null || getId().isEmpty()) {
            return;
        }

        String[] splittedId = getId().split("_");

        if (splittedId.length != 3) {
            log.warn(String.format("skandiabanken - unexpected array length, expected: %s, actual: %s - id: %s",
                    3, splittedId.length, getId()));
            return;
        }

        this.isin = splittedId[0];
        this.market = splittedId[1];
    }
}
