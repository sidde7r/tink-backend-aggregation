package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class AmountEntity {

    private String currency;
    private int minorUnits;

    public static AmountEntity fromAmount(final ExactCurrencyAmount amount) {
        return new AmountEntity(
                amount.getCurrencyCode(),
                amount.getExactValue().scaleByPowerOfTen(2).intValueExact());
    }
}
