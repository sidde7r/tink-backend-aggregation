package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities;

import se.tink.libraries.payment.enums.PaymentStatus;

public enum RedsysTransactionStatus {
    RCVD(PaymentStatus.PENDING),
    PDNG(PaymentStatus.PENDING),
    PATC(PaymentStatus.PENDING),
    ACTC(PaymentStatus.PAID),
    ACCP(PaymentStatus.PAID),
    ACFC(PaymentStatus.PAID),
    ACWC(PaymentStatus.PAID),
    ACSP(PaymentStatus.PAID),
    ACSC(PaymentStatus.PAID),
    ACCC(PaymentStatus.PAID),
    ACWP(PaymentStatus.PAID),
    PART(PaymentStatus.PAID),
    RJCT(PaymentStatus.REJECTED),
    CANC(PaymentStatus.CANCELLED);

    private PaymentStatus paymentStatus;

    RedsysTransactionStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentStatus getTinkStatus() {
        return paymentStatus;
    }
}
