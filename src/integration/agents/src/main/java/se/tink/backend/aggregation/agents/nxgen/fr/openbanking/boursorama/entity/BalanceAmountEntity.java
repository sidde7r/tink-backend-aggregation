package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAmountEntity {

    private String currency;
    private BigDecimal amount;

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
