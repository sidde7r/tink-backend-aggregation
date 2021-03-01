package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum FinecoBankPaymentStatus {
    ACCC("ACCC"),
    ACCP("ACCP"),
    ACSC("ACSC"),
    ACSP("ACSP"),
    ACTC("ACTC"),
    ACWC("ACWC"),
    ACWP("ACWP"),
    RCVD("RCVD"),
    PDNG("PDNG"),
    RJCT("RJCT"),
    CANC("CANC"),
    ACFC("ACFC"),
    PATC("PATC");

    private String value;

    private static EnumMap<FinecoBankPaymentStatus, PaymentStatus> finecoPaymentStatusToTinkMapper =
            new EnumMap<>(FinecoBankPaymentStatus.class);

    static {
        finecoPaymentStatusToTinkMapper.put(ACCC, PaymentStatus.PAID);
        finecoPaymentStatusToTinkMapper.put(ACCP, PaymentStatus.PENDING);
        finecoPaymentStatusToTinkMapper.put(ACSC, PaymentStatus.PAID);
        finecoPaymentStatusToTinkMapper.put(ACSP, PaymentStatus.PENDING);
        finecoPaymentStatusToTinkMapper.put(ACTC, PaymentStatus.PENDING);
        finecoPaymentStatusToTinkMapper.put(ACWC, PaymentStatus.PENDING);
        finecoPaymentStatusToTinkMapper.put(ACWP, PaymentStatus.PENDING);
        finecoPaymentStatusToTinkMapper.put(RCVD, PaymentStatus.CREATED);
        finecoPaymentStatusToTinkMapper.put(PDNG, PaymentStatus.PENDING);
        finecoPaymentStatusToTinkMapper.put(RJCT, PaymentStatus.REJECTED);
        finecoPaymentStatusToTinkMapper.put(CANC, PaymentStatus.CANCELLED);
        finecoPaymentStatusToTinkMapper.put(ACFC, PaymentStatus.PENDING);
        finecoPaymentStatusToTinkMapper.put(PATC, PaymentStatus.PENDING);
    }

    FinecoBankPaymentStatus(String value) {
        this.value = value;
    }

    private String getValue() {
        return value;
    }

    public static FinecoBankPaymentStatus fromString(String text) {
        return Arrays.stream(FinecoBankPaymentStatus.values())
                .filter(s -> s.value.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(ErrorMessages.FINECO_STATUS_MAPPING_ERROR));
    }

    public static PaymentStatus mapToTinkPaymentStatus(
            FinecoBankPaymentStatus finecoBankPaymentStatus) {
        return Optional.ofNullable(finecoPaymentStatusToTinkMapper.get(finecoBankPaymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format(
                                                ErrorMessages.MAPPING_STATUS_TO_TINK_STATUS_ERROR,
                                                finecoBankPaymentStatus.getValue())));
    }

    @Override
    public String toString() {
        return value;
    }
}
