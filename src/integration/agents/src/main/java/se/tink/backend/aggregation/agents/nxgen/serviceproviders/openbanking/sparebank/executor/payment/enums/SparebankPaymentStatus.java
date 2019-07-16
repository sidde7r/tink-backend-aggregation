package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum SparebankPaymentStatus {
    ACCP("ACCP"),
    ACSC("ACSC"),
    ACSP("ACSP"),
    ACTC("ACTC"),
    ACWC("ACWC"),
    ACWP("ACWP"),
    RCVD("RCVD"),
    PDNG("PDNG"),
    RJCT("RJCT"),
    CANC("CANC");

    private String statusText;

    private static EnumMap<SparebankPaymentStatus, PaymentStatus>
            sparebankPaymentStatusToTinkMapper =
                    new EnumMap<SparebankPaymentStatus, PaymentStatus>(
                            SparebankPaymentStatus.class);

    static {
        sparebankPaymentStatusToTinkMapper.put(ACCP, PaymentStatus.PENDING);
        sparebankPaymentStatusToTinkMapper.put(ACSC, PaymentStatus.PAID);
        sparebankPaymentStatusToTinkMapper.put(ACSP, PaymentStatus.PENDING);
        sparebankPaymentStatusToTinkMapper.put(ACTC, PaymentStatus.PENDING);
        sparebankPaymentStatusToTinkMapper.put(ACWC, PaymentStatus.PENDING);
        sparebankPaymentStatusToTinkMapper.put(ACWP, PaymentStatus.PENDING);
        sparebankPaymentStatusToTinkMapper.put(RCVD, PaymentStatus.CREATED);
        sparebankPaymentStatusToTinkMapper.put(PDNG, PaymentStatus.PENDING);
        sparebankPaymentStatusToTinkMapper.put(RJCT, PaymentStatus.REJECTED);
        sparebankPaymentStatusToTinkMapper.put(CANC, PaymentStatus.CANCELLED);
    }

    SparebankPaymentStatus(String status) {
        this.statusText = status;
    }

    public String getText() {
        return statusText;
    }

    public static SparebankPaymentStatus fromString(String text) {
        return Arrays.stream(SparebankPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Can not map " + text + " to Sparebank payment status"));
    }

    public static PaymentStatus mapToTinkPaymentStatus(
            SparebankPaymentStatus sparebankPaymentStatus) {
        return Optional.ofNullable(sparebankPaymentStatusToTinkMapper.get(sparebankPaymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Cannot map Sparebank payment status : "
                                                + sparebankPaymentStatus.toString()
                                                + " to Tink payment status"));
    }
}
