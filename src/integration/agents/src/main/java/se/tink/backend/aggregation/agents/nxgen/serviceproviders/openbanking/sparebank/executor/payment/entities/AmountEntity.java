package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private String amount;
    private String currency;

    public AmountEntity() {}

    public AmountEntity(String amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public static AmountEntity of(PaymentRequest paymentRequest) {
        Amount amount = paymentRequest.getPayment().getAmount();
        return new AmountEntity(amount.getValue().toString(), amount.getCurrency());
    }

    public Amount toTinkAmount() {
        return Amount.valueOf(
                currency, Double.valueOf(Double.parseDouble(amount) * 100).longValue(), 2);
    }
}
