package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountEntity {
    private BigDecimal granted;
    private BigDecimal drawn;
    private BigDecimal undrawn;
    private BigDecimal paid;
    private BigDecimal balance;

    @JsonIgnore
    public BigDecimal getGranted() {
        return granted;
    }

    @JsonIgnore
    public BigDecimal getDrawn() {
        return drawn;
    }

    @JsonIgnore
    public BigDecimal getUndrawn() {
        return undrawn;
    }

    @JsonIgnore
    public BigDecimal getPaid() {
        return paid;
    }

    @JsonIgnore
    public BigDecimal getBalance() {
        return balance.abs();
    }
}
