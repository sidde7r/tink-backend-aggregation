package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.entites;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {
    private String currency;
    private String amount;

    public AmountEntity(String currency, String amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public static AmountEntity amountOf(PaymentRequest paymentRequest) {
        ExactCurrencyAmount amount = paymentRequest.getPayment().getExactCurrencyAmount();
        return new AmountEntity(amount.getCurrencyCode(), String.valueOf(amount.getDoubleValue()));
    }
}
