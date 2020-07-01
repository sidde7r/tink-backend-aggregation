package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {

    private String currency;
    private String content;

    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(new BigDecimal(content), currency);
    }
}
