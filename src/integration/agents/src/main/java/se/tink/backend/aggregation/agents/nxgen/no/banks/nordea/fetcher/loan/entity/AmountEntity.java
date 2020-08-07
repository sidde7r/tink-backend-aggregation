package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.loan.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        return granted.abs();
    }

    public BigDecimal getBalance() {
        return balance.abs();
    }
}
