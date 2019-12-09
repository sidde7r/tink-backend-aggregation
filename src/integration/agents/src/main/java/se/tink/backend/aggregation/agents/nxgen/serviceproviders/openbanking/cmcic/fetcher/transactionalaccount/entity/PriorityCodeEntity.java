package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PriorityCodeEntity {
    HIGH("HIGH"),
    NORM("NORM");

    private String value;

    PriorityCodeEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static PriorityCodeEntity fromValue(String text) {
        for (PriorityCodeEntity b : PriorityCodeEntity.values()) {
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
