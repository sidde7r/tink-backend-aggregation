package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeeEntity {
    @JsonProperty("Administration")
    private double amdministration;
    @JsonProperty("Buy")
    private double buy;
    @JsonProperty("Sell")
    private double sell;
    @JsonProperty("Norman")
    private long norman;
    @JsonProperty("YearlyFee")
    private double yearly;

    public double getAmdministration() {
        return amdministration;
    }

    public void setAmdministration(double amdministration) {
        this.amdministration = amdministration;
    }

    public double getBuy() {
        return buy;
    }

    public void setBuy(double buy) {
        this.buy = buy;
    }

    public double getSell() {
        return sell;
    }

    public void setSell(double sell) {
        this.sell = sell;
    }

    public long getNorman() {
        return norman;
    }

    public void setNorman(long norman) {
        this.norman = norman;
    }

    public double getYearly() {
        return yearly;
    }

    public void setYearly(double yearly) {
        this.yearly = yearly;
    }
}
