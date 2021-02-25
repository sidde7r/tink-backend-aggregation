package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class InstructedAmountEntity {

    private String currency;
    private String amount;

    @JsonIgnore
    public static InstructedAmountEntity of(PaymentRequest paymentRequest) {
        return new InstructedAmountEntity(
                paymentRequest.getPayment().getExactCurrencyAmount().getCurrencyCode(),
                String.valueOf(
                        paymentRequest.getPayment().getExactCurrencyAmount().getDoubleValue()));
    }

    public InstructedAmountEntity() {}

    public InstructedAmountEntity(String currency, String amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }
}
