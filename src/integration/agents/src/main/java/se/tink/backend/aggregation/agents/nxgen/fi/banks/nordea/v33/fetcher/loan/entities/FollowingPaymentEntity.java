package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FollowingPaymentEntity {
    @JsonProperty
    private double instalment;
    @JsonProperty
    private double interest;
    @JsonProperty
    private int fees;
    @JsonProperty
    private double total;
    @JsonProperty
    private String date;

    public double getInstalment() {
        return instalment;
    }

    public double getInterest() {
        return interest;
    }

    public int getFees() {
        return fees;
    }

    public double getTotal() {
        return total;
    }

    public String getDate() {
        return date;
    }
}
