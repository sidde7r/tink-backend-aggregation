package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum BunqPaymentStatus {
    PENDING("PENDING"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED"),
    FINALIZED("FINALIZED"),
    UNKNOWN("UNKNOW");

    private String statusText;

    private static EnumMap<BunqPaymentStatus, PaymentStatus> bunqPaymentStatusToTinkMapper =
            new EnumMap<>(BunqPaymentStatus.class);

    static {
        bunqPaymentStatusToTinkMapper.put(FINALIZED, PaymentStatus.PAID);
        bunqPaymentStatusToTinkMapper.put(REJECTED, PaymentStatus.REJECTED);
        bunqPaymentStatusToTinkMapper.put(PENDING, PaymentStatus.PENDING);
        bunqPaymentStatusToTinkMapper.put(ACCEPTED, PaymentStatus.SIGNED);
        bunqPaymentStatusToTinkMapper.put(UNKNOWN, PaymentStatus.UNDEFINED);
    }

    BunqPaymentStatus(String status) {
        this.statusText = status;
    }

    public String getText() {
        return this.statusText;
    }

    public static BunqPaymentStatus fromString(String text) {
        return Arrays.stream(BunqPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(BunqPaymentStatus bunqPaymentStatus) {
        return Optional.ofNullable(bunqPaymentStatusToTinkMapper.get(bunqPaymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot map Bunq payment status : "
                                                + bunqPaymentStatus.toString()
                                                + " to Tink payment status."));
    }

    @Override
    public String toString() {
        return statusText;
    }
}
