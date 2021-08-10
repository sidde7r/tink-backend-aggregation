package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonObject
public class AmountEntity {
    private BigDecimal value;
    private String currency;

    public AmountEntity(ExactCurrencyAmount currencyAmount) {
        this.value = BigDecimal.valueOf(currencyAmount.getDoubleValue());
        this.currency = currencyAmount.getCurrencyCode();
    }

    @JsonIgnore
    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(value, currency);
    }
}
