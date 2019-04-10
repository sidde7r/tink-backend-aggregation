package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditEntity {
    @JsonProperty private double limit;
    @JsonProperty private double available;
    @JsonProperty private double spent;

    public double getLimit() {
        return limit;
    }

    public double getAvailable() {
        return available;
    }

    public double getSpent() {
        return spent;
    }
}
