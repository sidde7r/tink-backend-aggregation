package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.entities;

import java.math.BigDecimal;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransferBodyEntity {

    private static final String CREDITOR_NAME = "Payment Creditor";

    private String currency;
    private String lifetimeAmount;
    private InstructionsEntity instructions;

    public static TransferBodyEntity of(PaymentRequest paymentRequest, AccountEntity creditor) {
        return new TransferBodyEntity(
                paymentRequest.getPayment().getCurrency(),
                String.valueOf(
                        paymentRequest.getPayment().getExactCurrencyAmount().getExactValue()),
                new InstructionsEntity(
                        Collections.singletonList(
                                new TransferDestinationEntity(
                                        creditor,
                                        new CustomerDataEntity(
                                                Collections.singletonList(CREDITOR_NAME))))));
    }

    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(new BigDecimal(lifetimeAmount), currency);
    }
}
