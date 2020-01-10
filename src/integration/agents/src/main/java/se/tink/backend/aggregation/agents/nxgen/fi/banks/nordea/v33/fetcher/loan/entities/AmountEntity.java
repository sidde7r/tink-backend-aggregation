package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountEntity {
    @JsonProperty private BigDecimal granted;
    @JsonProperty private BigDecimal drawn;
    @JsonProperty private BigDecimal undrawn;
    @JsonProperty private BigDecimal paid;
    @JsonProperty private BigDecimal balance;

    public BigDecimal getGranted() {
        return granted.abs();
    }

    public BigDecimal getDrawn() {
        return drawn;
    }

    public BigDecimal getUndrawn() {
        return undrawn;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public BigDecimal getBalance() {
        return balance.abs();
    }
}
