package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {
    private String currency;
    private String amount;

    public AmountEntity() {}

    public AmountEntity(String currency, String amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public static AmountEntity amountOf(PaymentRequest paymentRequest) {
        return new AmountEntity(
                paymentRequest.getPayment().getCurrency(),
                String.valueOf(paymentRequest.getPayment().getAmount().getValue()));
    }

    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(new BigDecimal(amount), currency);
    }

    public String getCurrency() {
        return currency;
    }
}
