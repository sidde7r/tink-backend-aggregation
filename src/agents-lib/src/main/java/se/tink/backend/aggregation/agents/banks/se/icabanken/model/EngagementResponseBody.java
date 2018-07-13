package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EngagementResponseBody {

    @JsonProperty("HasDepots")
    private boolean hasDepots;

    @JsonProperty("HasLoans")
    private boolean hasLoans;

    @JsonProperty("HasInsurances")
    private boolean hasInsurances;

    public boolean hasDepots() {
        return hasDepots;
    }

    public void setHasDepots(boolean hasDepots) {
        this.hasDepots = hasDepots;
    }

    public boolean hasLoans() {
        return hasLoans;
    }

    public void setHasLoans(boolean hasLoans) {
        this.hasLoans = hasLoans;
    }

    public boolean hasInsurances() {
        return hasInsurances;
    }

    public void setHasInsurances(boolean hasInsurances) {
        this.hasInsurances = hasInsurances;
    }
}
