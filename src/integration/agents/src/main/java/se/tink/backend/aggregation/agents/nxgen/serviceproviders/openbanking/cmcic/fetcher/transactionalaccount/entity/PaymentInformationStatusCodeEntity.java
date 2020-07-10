package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentInformationStatusCodeEntity {
    ACCP("ACCP"),
    ACSC("ACSC"),
    ACSP("ACSP"),
    ACTC("ACTC"),
    ACWC("ACWC"),
    ACWP("ACWP"),
    PART("PART"),
    RCVD("RCVD"),
    PDNG("PDNG"),
    CANC("CANC"),
    RJCT("RJCT");

    private String value;

    PaymentInformationStatusCodeEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static PaymentInformationStatusCodeEntity fromValue(String text) {
        for (PaymentInformationStatusCodeEntity b : PaymentInformationStatusCodeEntity.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
