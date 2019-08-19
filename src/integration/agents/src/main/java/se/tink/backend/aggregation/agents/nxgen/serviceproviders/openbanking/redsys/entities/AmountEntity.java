package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {
    @JsonProperty private String currency;
    @JsonProperty private String amount;

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(
                BigDecimal.valueOf(StringUtils.parseAmount(amount)), currency);
    }

    public static AmountEntity withAmount(ExactCurrencyAmount amount) {
        AmountEntity entity = new AmountEntity();
        entity.currency = amount.getCurrencyCode();
        entity.amount = amount.getExactValue().toPlainString();
        return entity;
    }
}
