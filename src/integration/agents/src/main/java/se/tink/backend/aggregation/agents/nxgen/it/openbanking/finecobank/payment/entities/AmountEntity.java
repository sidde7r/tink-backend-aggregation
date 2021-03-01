package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AmountEntity {

    private BigDecimal amount;
    private String currency;

    public AmountEntity(ExactCurrencyAmount exactCurrencyAmount) {
        this.amount = exactCurrencyAmount.getExactValue();
        this.currency = exactCurrencyAmount.getCurrencyCode();
    }

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }
}
