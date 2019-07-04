package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundsEntity {

    @JsonProperty("AllocationPercent")
    private double allocationPercent;

    @JsonProperty("AquisitionValue")
    private double acquisitionValue;

    @JsonProperty("BlockedForTrade")
    private boolean blockedForTrade;

    @JsonProperty("BlockedForTradeFrom")
    private String blockedForTradeFrom;

    @JsonProperty("BlockedForTradeTo")
    private String blockedForTradeTo;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Disclaimer")
    private String disclaimer;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Is4pCertified")
    private boolean is4PCertified;

    @JsonProperty("MarketValue")
    private double marketValue;

    @JsonProperty("MorningstarPerformanceId")
    private String morningstarPerformanceId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Performance")
    private double performance;

    @JsonProperty("PerformanceSEK")
    private double performancesek;

    @JsonProperty("Rate")
    private double rate;

    @JsonProperty("RateDate")
    private String rateDate;

    @JsonProperty("Shares")
    private double shares;

    public double getAcquisitionValue() {
        return acquisitionValue;
    }

    public String getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public String getName() {
        return name;
    }

    public double getPerformance() {
        return performance;
    }

    public double getPerformancesek() {
        return performancesek;
    }

    public double getRate() {
        return rate;
    }

    public String getRateDate() {
        return rateDate;
    }

    public double getShares() {
        return shares;
    }
}
