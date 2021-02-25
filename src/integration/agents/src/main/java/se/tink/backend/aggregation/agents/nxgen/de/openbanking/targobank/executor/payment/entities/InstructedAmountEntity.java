package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class InstructedAmountEntity {
    private String currency;
    private String amount;

    @JsonCreator
    public InstructedAmountEntity(
            @JsonProperty("currency") String currency, @JsonProperty("amount") String amount) {
        this.currency = currency;
        this.amount = amount;
    }

    @JsonIgnore
    public static InstructedAmountEntity of(PaymentRequest paymentRequest) {
        return new InstructedAmountEntity(
                paymentRequest.getPayment().getExactCurrencyAmount().getCurrencyCode(),
                String.valueOf(
                        paymentRequest.getPayment().getExactCurrencyAmount().getDoubleValue()));
    }

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }
}
