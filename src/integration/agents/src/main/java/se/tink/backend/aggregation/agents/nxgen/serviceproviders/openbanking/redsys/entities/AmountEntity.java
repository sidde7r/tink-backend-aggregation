package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {
    @JsonProperty private String currency;
    @JsonProperty private BigDecimal amount;

    public AmountEntity() {}

    public AmountEntity(ExactCurrencyAmount amount) {
        this.currency = amount.getCurrencyCode();
        this.amount = amount.getExactValue();
    }

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
