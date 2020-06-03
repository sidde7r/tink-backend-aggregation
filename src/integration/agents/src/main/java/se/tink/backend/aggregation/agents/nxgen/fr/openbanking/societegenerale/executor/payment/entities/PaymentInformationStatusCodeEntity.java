package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum PaymentInformationStatusCodeEntity {
    ACCP("ACCP"),
    ACSC("ACSC"),
    ACSP("ACSP"),
    ACTC("ACTC"),
    ACWC("ACWC"),
    ACWP("ACWP"),
    CANC("CANC"),
    PART("PART"),
    RCVD("RCVD"),
    PDNG("PDNG"),
    RJCT("RJCT"),
    UNKNOWN("UNKNOWN");

    private String value;

    PaymentInformationStatusCodeEntity(String value) {
        this.value = value;
    }

    private static EnumMap<PaymentInformationStatusCodeEntity, PaymentStatus>
            paymentStatusToTinkMapper = new EnumMap<>(PaymentInformationStatusCodeEntity.class);

    static {
        paymentStatusToTinkMapper.put(ACCP, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACSC, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACSP, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACTC, PaymentStatus.PENDING);
        paymentStatusToTinkMapper.put(ACWC, PaymentStatus.PENDING);
        paymentStatusToTinkMapper.put(ACWP, PaymentStatus.PENDING);
        paymentStatusToTinkMapper.put(PART, PaymentStatus.PENDING);
        paymentStatusToTinkMapper.put(RCVD, PaymentStatus.CREATED);
        paymentStatusToTinkMapper.put(RJCT, PaymentStatus.REJECTED);
        paymentStatusToTinkMapper.put(CANC, PaymentStatus.CANCELLED);
        paymentStatusToTinkMapper.put(PDNG, PaymentStatus.SIGNED);
    }

    @JsonCreator
    public static PaymentInformationStatusCodeEntity fromValue(String text) {
        return Arrays.stream(PaymentInformationStatusCodeEntity.values())
                .filter(s -> s.value.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public PaymentStatus mapToTinkPaymentStatus() {
        return Optional.ofNullable(
                        paymentStatusToTinkMapper.get(
                                Enum.valueOf(PaymentInformationStatusCodeEntity.class, value)))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                SocieteGeneraleConstants.ErrorMessages.MAPPING,
                                                value.toString())));
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
