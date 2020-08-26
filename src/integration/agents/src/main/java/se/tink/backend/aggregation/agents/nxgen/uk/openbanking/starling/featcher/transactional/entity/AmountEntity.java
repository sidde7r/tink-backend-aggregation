package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {

    private String currency;
    private Long minorUnits;

    public ExactCurrencyAmount toExactCurrencyAmount() {
        return ExactCurrencyAmount.of(BigDecimal.valueOf(minorUnits, 2), currency);
    }
}
