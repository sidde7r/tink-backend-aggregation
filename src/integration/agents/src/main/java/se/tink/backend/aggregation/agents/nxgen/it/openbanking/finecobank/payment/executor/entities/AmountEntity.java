package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private String currency;
    private Double amount;

    @JsonCreator
    public AmountEntity(
            @JsonProperty("currency") String currency, @JsonProperty("amount") Double amount) {
        this.currency = currency;
        this.amount = amount;
    }

    @JsonIgnore
    public static AmountEntity of(PaymentRequest paymentRequest) {
        return new AmountEntity(
                paymentRequest.getPayment().getAmount().getCurrency(),
                paymentRequest.getPayment().getAmount().getValue());
    }

    @JsonIgnore
    public Amount toTinkAmount() {
        return Amount.valueOf(currency, (long) (amount * 100), 2);
    }
}
