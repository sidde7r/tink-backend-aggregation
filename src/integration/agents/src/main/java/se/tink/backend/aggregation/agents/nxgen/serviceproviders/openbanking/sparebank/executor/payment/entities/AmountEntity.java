package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class AmountEntity {
    private String amount;
    @Getter private String currency;

    @JsonIgnore
    public static AmountEntity amountOf(PaymentRequest paymentRequest) {
        ExactCurrencyAmount amount = paymentRequest.getPayment().getExactCurrencyAmount();
        return new AmountEntity(
                String.format("%.2f", amount.getExactValue()), amount.getCurrencyCode());
    }

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(getParsedAmount(), currency);
    }

    @JsonIgnore
    private double getParsedAmount() {
        return StringUtils.parseAmount(amount);
    }
}
