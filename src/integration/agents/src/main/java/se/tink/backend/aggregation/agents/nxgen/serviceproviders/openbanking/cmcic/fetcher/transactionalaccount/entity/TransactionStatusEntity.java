package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionStatusEntity {
    BOOK("BOOK"),
    PDNG("PDNG"),
    OTHR("OTHR");

    private String value;

    TransactionStatusEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TransactionStatusEntity fromValue(String text) {
        for (TransactionStatusEntity b : TransactionStatusEntity.values()) {
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
