package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AmountEntity {

    private String amount;
    private String currency;

    public static AmountEntity amountOf(PaymentRequest paymentRequest) {
        return new AmountEntity(
                String.valueOf(
                        paymentRequest.getPayment().getExactCurrencyAmount().getExactValue()),
                paymentRequest.getPayment().getCurrency());
    }

    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(new BigDecimal(amount), currency);
    }
}
