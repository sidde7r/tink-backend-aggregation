package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    @JsonProperty private String currency;
    @JsonProperty private String amount;

    @JsonIgnore
    public Amount toTinkAmount() {
        return new Amount(currency, new BigDecimal(amount));
    }

    public static AmountEntity withAmount(Amount amount) {
        AmountEntity entity = new AmountEntity();
        entity.currency = amount.getCurrency();
        entity.amount = amount.toBigDecimal().toPlainString();
        return entity;
    }
}
