package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Action {

    @JsonProperty("payments")
    private Payments payments;

    public void setPayments(Payments payments) {
        this.payments = payments;
    }

    public Payments getPayments() {
        return payments;
    }
}
