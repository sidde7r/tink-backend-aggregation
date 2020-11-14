package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class AmountEntity {
    private String currency;
    private String amount;

    public ExactCurrencyAmount toAmount() {
        if (isNotEmpty()) {
            return ExactCurrencyAmount.of(Double.parseDouble(amount), currency);
        } else {
            throw new IllegalStateException("Couldn't parse amount: '" + amount + "'");
        }
    }

    public boolean isNotEmpty() {
        return NumberUtils.isParsable(amount);
    }
}
