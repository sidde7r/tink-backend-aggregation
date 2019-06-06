package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum SBABPaymentStatus {
    PROCESSING("processing"),
    NEED_SIGNATURE("need_sign"),
    SIGNATURE_FAILED("signature_failed"),
    TRANSFER_REQUEST_FAILED("transfer_request_failed"),
    TRANSFER_REQUEST_ERROR("transfer_request_error"),
    CREATED("created"),
    DELETE_REQUEST_FAILED("delete_request_failed"),
    DELETED("deleted"),
    UNKNOWN("unknown");

    private static EnumMap<SBABPaymentStatus, PaymentStatus> sbabPaymentStatusToTinkMapper =
            new EnumMap<>(SBABPaymentStatus.class);

    static {
        sbabPaymentStatusToTinkMapper.put(CREATED, PaymentStatus.PAID);
        sbabPaymentStatusToTinkMapper.put(SIGNATURE_FAILED, PaymentStatus.REJECTED);
        sbabPaymentStatusToTinkMapper.put(NEED_SIGNATURE, PaymentStatus.PENDING);
        sbabPaymentStatusToTinkMapper.put(CREATED, PaymentStatus.SIGNED);
        sbabPaymentStatusToTinkMapper.put(UNKNOWN, PaymentStatus.UNDEFINED);
    }

    private String statusText;

    SBABPaymentStatus(String statusText) {
        this.statusText = statusText;
    }

    public static SBABPaymentStatus fromString(String text) {
        return Arrays.stream(SBABPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(SBABPaymentStatus sbabPaymentStatus) {
        return Optional.ofNullable(sbabPaymentStatusToTinkMapper.get(sbabPaymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map SBAB payment status : "
                                                + sbabPaymentStatus.toString()
                                                + " to Tink payment status."));
    }

    @Override
    public String toString() {
        return statusText;
    }
}
