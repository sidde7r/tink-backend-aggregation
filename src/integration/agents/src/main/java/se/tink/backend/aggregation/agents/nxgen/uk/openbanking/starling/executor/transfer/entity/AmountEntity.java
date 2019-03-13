package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {

    private static final int UNIT_SCALE = 2;

    private String currency;
    private long minorUnits;

    public AmountEntity(String currency, long minorUnits) {
        this.currency = currency;
        this.minorUnits = minorUnits;
    }

    public static AmountEntity fromAmount(final Amount amount) {

        long unscaledValue = amount.toBigDecimal().movePointRight(UNIT_SCALE).longValue();
        return new AmountEntity(amount.getCurrency(), unscaledValue);
    }
}
