package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AmountEntity {
    private String amount;
    private String currency;

    public AmountEntity() {}

    public AmountEntity(Amount amount) {
        this.amount = amount.getValue().toString();
        this.currency = amount.getCurrency();
    }

    public Amount toTinkAmount() {
        return new Amount(currency, Double.parseDouble(amount));
    }
}
