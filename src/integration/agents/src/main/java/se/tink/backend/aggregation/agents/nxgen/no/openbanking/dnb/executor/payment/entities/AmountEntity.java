package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {
    private String amount;
    private String currency;

    public AmountEntity() {}

    public AmountEntity(double amount, String currency) {
        this.amount = String.valueOf(amount);
        this.currency = currency;
    }

    public static AmountEntity amountOf(PaymentRequest paymentRequest) {
        return new AmountEntity(
                paymentRequest.getPayment().getExactCurrencyAmount().getDoubleValue(),
                paymentRequest.getPayment().getExactCurrencyAmount().getCurrencyCode());
    }

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(getParsedAmount(), currency);
    }

    @JsonIgnore
    private double getParsedAmount() {
        return StringUtils.parseAmount(amount);
    }

    public String getCurrency() {
        return currency;
    }
}
