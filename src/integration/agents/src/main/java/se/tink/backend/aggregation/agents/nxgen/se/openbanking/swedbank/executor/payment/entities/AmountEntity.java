package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {
    private String amount;
    private String currency;

    public AmountEntity(double amount, String currency) {
        this.amount = String.valueOf(amount);
        this.currency = currency;
    }

    public double getParsedAmount() {
        return StringUtils.parseAmount(amount);
    }

    public String getCurrency() {
        return currency;
    }

    public AmountEntity() {}
}
