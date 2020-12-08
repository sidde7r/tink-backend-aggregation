package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {
    private String amount;
    private String currency;

    @JsonCreator
    private AmountEntity(
            @JsonProperty("amount") BigDecimal amount, @JsonProperty("currency") String currency) {
        this.amount = amount.toString();
        this.currency = currency;
    }

    @JsonIgnore
    public static AmountEntity amountOf(PaymentRequest paymentRequest) {
        final ExactCurrencyAmount amount = paymentRequest.getPayment().getExactCurrencyAmount();
        return new AmountEntity(amount.getExactValue(), amount.getCurrencyCode());
    }

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(BigDecimal.valueOf(getParsedAmount()), currency);
    }

    @JsonIgnore
    private double getParsedAmount() {
        return StringUtils.parseAmount(amount);
    }

    public String getCurrency() {
        return currency;
    }
}
