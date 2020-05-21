package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {
    private String currency;
    private double amount;

    public AmountEntity(String currency, double amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public static AmountEntity amountOf(PaymentRequest paymentRequest) {
        ExactCurrencyAmount amount = paymentRequest.getPayment().getExactCurrencyAmount();
        return new AmountEntity(amount.getCurrencyCode(), amount.getDoubleValue());
    }
}
