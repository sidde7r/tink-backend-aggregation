package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PurposeCodeEntity {
    ACCT("ACCT"),
    CASH("CASH"),
    COMC("COMC"),
    CPKC("CPKC"),
    TRPT("TRPT");

    private String value;

    PurposeCodeEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static PurposeCodeEntity fromValue(String text) {
        for (PurposeCodeEntity b : PurposeCodeEntity.values()) {
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
