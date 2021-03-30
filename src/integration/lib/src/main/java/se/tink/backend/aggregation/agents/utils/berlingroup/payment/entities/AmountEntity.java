package se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AmountEntity {

    private String amount;
    private String currency;

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(StringUtils.parseAmount(amount), currency);
    }
}
