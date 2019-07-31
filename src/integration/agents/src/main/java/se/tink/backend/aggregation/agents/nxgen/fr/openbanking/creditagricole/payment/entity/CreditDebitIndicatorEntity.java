package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CreditDebitIndicatorEntity {
    CRDT("CRDT"),
    DBIT("DBIT");

    private String value;

    CreditDebitIndicatorEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static CreditDebitIndicatorEntity fromValue(String text) {
        for (CreditDebitIndicatorEntity b : CreditDebitIndicatorEntity.values()) {
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
