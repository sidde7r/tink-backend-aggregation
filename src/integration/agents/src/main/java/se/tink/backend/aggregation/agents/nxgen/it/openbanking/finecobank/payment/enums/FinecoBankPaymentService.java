package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.libraries.payment.rpc.Payment;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FinecoBankPaymentService {
    SINGLE("payments"),
    PERIODIC("periodic-payments");

    private final String value;

    public static FinecoBankPaymentService fromTinkPayment(Payment payment) {
        switch (payment.getPaymentServiceType()) {
            case SINGLE:
                return SINGLE;
            case PERIODIC:
                return PERIODIC;
            default:
                throw new IllegalArgumentException("Unsupported tink PaymentServiceType!");
        }
    }
}
