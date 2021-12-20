package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum OpBankPaymentStatus {
    UNAUTHORIZED("Unauthorized", PaymentStatus.CREATED),
    ACCEPTED("Accepted", PaymentStatus.CREATED),
    PENDING("Pending", PaymentStatus.PENDING),
    ERROR("Error", PaymentStatus.CANCELLED),
    SUBMITTED("Submitted", PaymentStatus.PAID),
    COMPLETED("Completed", PaymentStatus.SIGNED),
    CREDITED("Credited", PaymentStatus.SETTLEMENT_COMPLETED),
    REJECTED("Rejected", PaymentStatus.REJECTED),
    AUTHORIZED("Authorized", PaymentStatus.SIGNED);

    private String statusText;
    private PaymentStatus paymentStatus;

    OpBankPaymentStatus(String status, PaymentStatus paymentStatus) {
        this.statusText = status;
        this.paymentStatus = paymentStatus;
    }

    public static OpBankPaymentStatus fromString(String text) {
        return Arrays.stream(OpBankPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(ERROR);
    }

    public String getText() {
        return this.statusText;
    }

    @Override
    public String toString() {
        return statusText;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
}
