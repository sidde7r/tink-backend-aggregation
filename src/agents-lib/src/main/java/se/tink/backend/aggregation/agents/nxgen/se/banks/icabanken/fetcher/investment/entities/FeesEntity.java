package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FeesEntity {
    @JsonProperty("Administration")
    private double administration;
    @JsonProperty("Buy")
    private double buy;
    @JsonProperty("Sell")
    private double sell;
    @JsonProperty("Norman")
    private int norman;
    @JsonProperty("YearlyFee")
    private double yearlyFee;

    public double getAdministration() {
        return administration;
    }

    public double getBuy() {
        return buy;
    }

    public double getSell() {
        return sell;
    }

    public int getNorman() {
        return norman;
    }

    public double getYearlyFee() {
        return yearlyFee;
    }
}
