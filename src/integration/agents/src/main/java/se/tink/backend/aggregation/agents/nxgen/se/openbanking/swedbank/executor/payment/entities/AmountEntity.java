package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {
    private String amount;
    private String currency;

    @JsonCreator
    private AmountEntity(
            @JsonProperty("amount") double amount, @JsonProperty("currency") String currency) {
        this.amount = String.valueOf(amount);
        this.currency = currency;
    }

    @JsonIgnore
    public static AmountEntity amountOf(PaymentRequest paymentRequest) {
        Amount amount = paymentRequest.getPayment().getAmount();
        return new AmountEntity(amount.getValue(), amount.getCurrency());
    }

    @JsonIgnore
    public Amount toTinkAmount() {
        return Amount.valueOf(currency, Double.valueOf(getParsedAmount() * 100).longValue(), 2);
    }

    @JsonIgnore
    private double getParsedAmount() {
        return StringUtils.parseAmount(amount);
    }

    public String getCurrency() {
        return currency;
    }
}
