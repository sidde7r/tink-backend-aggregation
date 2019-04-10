package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundHoldingsEntity {
    @JsonProperty("FundName")
    private String fundName;

    @JsonProperty("FundId")
    private String fundId;

    @JsonProperty("Shares")
    private double shares;

    @JsonProperty("Rate")
    private double rate;

    @JsonProperty("InvestedAmount")
    private double investedAmount;

    @JsonProperty("MarketValue")
    private double marketValue;

    @JsonProperty("ReturnAmount")
    private double returnAmount;

    @JsonProperty("ReturnPercentage")
    private double returnPercentage;

    public String getFundName() {
        return fundName;
    }

    public String getFundId() {
        return fundId;
    }

    public double getShares() {
        return shares;
    }

    public double getRate() {
        return rate;
    }

    public double getInvestedAmount() {
        return investedAmount;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public double getReturnAmount() {
        return returnAmount;
    }

    public double getReturnPercentage() {
        return returnPercentage;
    }
}
