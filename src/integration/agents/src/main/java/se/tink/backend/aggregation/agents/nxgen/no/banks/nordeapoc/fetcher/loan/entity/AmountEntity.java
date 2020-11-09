package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.loan.entity;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AmountEntity {
    private BigDecimal granted;
    private BigDecimal paid;
    private BigDecimal balance;

    public BigDecimal getGranted() {
        return granted != null ? granted.abs() : null;
    }

    public BigDecimal getBalance() {
        return balance != null ? balance.abs() : null;
    }
}
