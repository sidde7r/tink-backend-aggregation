package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {
    private String currency;
    private String amount;

    public AmountEntity(String currency, String amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public static AmountEntity amountOf(PaymentRequest paymentRequest) {
        return new AmountEntity(
                paymentRequest.getPayment().getCurrency(),
                String.valueOf(paymentRequest.getPayment().getAmount().getValue()));
    }

    public Amount toTinkAmount() {
        return Amount.valueOf(currency, Double.valueOf(doubleValue() * 100).longValue(), 2);
    }

    public AmountEntity() {}

    public String getCurrency() {
        return currency;
    }

    private double doubleValue() {
        return StringUtils.parseAmount(amount);
    }
}
