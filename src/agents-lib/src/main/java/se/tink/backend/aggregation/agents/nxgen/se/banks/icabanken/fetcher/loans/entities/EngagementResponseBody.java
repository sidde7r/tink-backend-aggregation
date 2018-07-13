package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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

    public boolean hasLoans() {
        return hasLoans;
    }

    public boolean hasInsurances() {
        return hasInsurances;
    }
}
