package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {
    @JsonProperty private String currency;
    @JsonProperty private String amount;

    @JsonIgnore
    public Amount toTinkAmount() {
        // decimal separator is inconsistent between banks
        return new Amount(currency, StringUtils.parseAmount(amount));
    }

    public static AmountEntity withAmount(Amount amount) {
        // FIXME: which decimal separator to use?
        AmountEntity entity = new AmountEntity();
        entity.currency = amount.getCurrency();
        entity.amount = amount.toBigDecimal().toPlainString();
        return entity;
    }
}
