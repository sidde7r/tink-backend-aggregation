package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary;

import se.tink.libraries.payment.enums.PaymentStatus;

public enum SibsTransactionStatus {
    RCVD(PaymentStatus.PENDING),
    PDNG(PaymentStatus.PENDING),
    PATC(PaymentStatus.PENDING),
    ACTC(PaymentStatus.PENDING),
    ACCP(PaymentStatus.PENDING),
    ACFC(PaymentStatus.PENDING),
    ACWC(PaymentStatus.PENDING),
    ACSP(PaymentStatus.PAID),
    ACSC(PaymentStatus.PAID),
    ACCC(PaymentStatus.PAID),
    RJC(PaymentStatus.REJECTED),
    CANC(PaymentStatus.CANCELLED);

    private PaymentStatus paymentStatus;

    SibsTransactionStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentStatus getTinkStatus() {
        return paymentStatus;
    }

    public boolean isWaitingStatus() {
        return this.getTinkStatus() == PaymentStatus.PENDING;
    }
}
