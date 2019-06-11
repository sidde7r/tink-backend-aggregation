package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FrequencyCodeEntity {
    MNTH("MNTH"),
    TOMN("TOMN"),
    QUTR("QUTR"),
    SEMI("SEMI"),
    YEAR("YEAR");

    private String value;

    FrequencyCodeEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static FrequencyCodeEntity fromValue(String text) {
        for (FrequencyCodeEntity b : FrequencyCodeEntity.values()) {
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
