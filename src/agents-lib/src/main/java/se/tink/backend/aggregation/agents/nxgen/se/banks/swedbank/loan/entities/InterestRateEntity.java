package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestRateEntity {
    private String current;

    public String getCurrent() {
        return current;
    }
}
