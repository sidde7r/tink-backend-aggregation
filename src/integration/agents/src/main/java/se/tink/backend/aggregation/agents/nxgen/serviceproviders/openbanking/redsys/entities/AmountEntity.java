package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {
    private String currency;
    private String amount;

    public static AmountEntity withAmount(ExactCurrencyAmount amount) {
        AmountEntity entity = new AmountEntity();
        entity.currency = amount.getCurrencyCode();
        entity.amount = amount.getExactValue().toPlainString();
        return entity;
    }

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(
                BigDecimal.valueOf(StringUtils.parseAmount(amount)), currency);
    }
}
