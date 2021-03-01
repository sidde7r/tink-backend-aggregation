package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FinecoBankPaymentProduct {
    SEPA_CREDIT_TRANSFER("sepa-credit-transfers"),
    INSTANT_SEPA_CREDIT_TRANSFER("instant-sepa-credit-transfers"),
    CROSS_BORDER_CREDIT_TRANSFER("cross-border-credit-transfers");

    private final String value;

    public static FinecoBankPaymentProduct fromTinkPayment(Payment payment) {
        if (payment.getPaymentScheme() == PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER) {
            return INSTANT_SEPA_CREDIT_TRANSFER;
        } else if (payment.isSepa()) {
            return SEPA_CREDIT_TRANSFER;
        } else {
            return CROSS_BORDER_CREDIT_TRANSFER;
        }
    }
}
