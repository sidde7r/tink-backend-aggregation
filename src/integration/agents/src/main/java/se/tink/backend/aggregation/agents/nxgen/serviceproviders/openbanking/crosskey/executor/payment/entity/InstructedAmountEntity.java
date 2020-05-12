package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
@JsonNaming(UpperCamelCaseStrategy.class)
public class InstructedAmountEntity {

    private String amount;

    private String currency;

    public InstructedAmountEntity() {}

    public InstructedAmountEntity(String amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    @JsonIgnore
    public static InstructedAmountEntity of(PaymentRequest paymentRequest) {
        String amount = paymentRequest.getPayment().getAmount().getValue().toString();
        String currency = paymentRequest.getPayment().getAmount().getCurrency();
        return new InstructedAmountEntity(amount, currency);
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
