package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestsEntity {
    @JsonProperty("Accrued")
    private double accrued;

    @JsonProperty("Interest")
    private double interest;

    public double getAccrued() {
        return accrued;
    }

    public double getInterest() {
        return interest;
    }
}
