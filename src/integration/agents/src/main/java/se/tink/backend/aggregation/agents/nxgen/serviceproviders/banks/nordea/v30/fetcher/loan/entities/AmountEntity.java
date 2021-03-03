package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AmountEntity {
    private BigDecimal granted;
    private BigDecimal drawn;
    private BigDecimal undrawn;
    private BigDecimal paid;
    private BigDecimal balance;
}
