package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountEntity {
    @JsonProperty private double granted;
    @JsonProperty private double drawn;
    @JsonProperty private double undrawn;
    @JsonProperty private double paid;
    @JsonProperty private double balance;

    public double getGranted() {
        return granted;
    }

    public double getDrawn() {
        return drawn;
    }

    public double getUndrawn() {
        return undrawn;
    }

    public double getPaid() {
        return paid;
    }

    public double getBalance() {
        return balance;
    }
}
