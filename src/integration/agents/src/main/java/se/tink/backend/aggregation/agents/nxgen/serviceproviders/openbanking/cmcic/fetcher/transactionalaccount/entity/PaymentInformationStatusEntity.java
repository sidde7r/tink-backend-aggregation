package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum PaymentInformationStatusEntity {
    ACCP("ACCP", PaymentStatus.SIGNED),
    ACSC("ACSC", PaymentStatus.SIGNED),
    ACSP("ACSP", PaymentStatus.SIGNED),
    ACTC("ACTC", PaymentStatus.PENDING),
    ACWC("ACWC", PaymentStatus.PENDING),
    ACWP("ACWP", PaymentStatus.PENDING),
    PART("PART", PaymentStatus.PENDING),
    RCVD("RCVD", PaymentStatus.CREATED),
    PDNG("PDNG", PaymentStatus.SIGNED),
    CANC("CANC", PaymentStatus.CANCELLED),
    RJCT("RJCT", PaymentStatus.REJECTED),
    UNKNOWN("UNKNOWN", PaymentStatus.UNDEFINED);

    private String value;
    private PaymentStatus paymentStatus;

    PaymentInformationStatusEntity(String value, PaymentStatus paymentStatus) {
        this.value = value;
        this.paymentStatus = paymentStatus;
    }

    public String getValue() {
        return value;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    @JsonCreator
    public static PaymentInformationStatusEntity fromValue(String text) {
        return Arrays.stream(PaymentInformationStatusEntity.values())
                .filter(status -> status.value.equals(text))
                .findFirst()
                .orElse(null);
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
