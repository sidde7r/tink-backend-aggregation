package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.enums;

import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum SBABPaymentStatus {
    PROCESSING("processing", PaymentStatus.PENDING),
    NEED_SIGNATURE("need_sign", PaymentStatus.PENDING),
    SIGNATURE_FAILED("signature_failed", PaymentStatus.REJECTED),
    TRANSFER_REQUEST_FAILED("transfer_request_failed", PaymentStatus.REJECTED),
    TRANSFER_REQUEST_ERROR("transfer_request_error", PaymentStatus.REJECTED),
    CREATED("created", PaymentStatus.PAID),
    DELETE_REQUEST_FAILED("delete_request_failed", PaymentStatus.CANCELLED),
    DELETED("deleted", PaymentStatus.CANCELLED),
    UNKNOWN("unknown", PaymentStatus.UNDEFINED);

    private String statusText;
    private PaymentStatus paymentStatus;

    SBABPaymentStatus(String statusText, PaymentStatus paymentStatus) {
        this.statusText = statusText;
        this.paymentStatus = paymentStatus;
    }

    public static SBABPaymentStatus fromString(String text) {
        return Arrays.stream(SBABPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    @Override
    public String toString() {
        return statusText;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
}
