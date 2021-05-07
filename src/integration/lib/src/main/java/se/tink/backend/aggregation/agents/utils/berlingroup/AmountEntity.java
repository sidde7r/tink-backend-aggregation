package se.tink.backend.aggregation.agents.utils.berlingroup;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Setter
public class AmountEntity {
    private String currency;
    private BigDecimal amount;

    public AmountEntity(ExactCurrencyAmount exactCurrencyAmount) {
        this.amount = exactCurrencyAmount.getExactValue();
        this.currency = exactCurrencyAmount.getCurrencyCode();
    }

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
