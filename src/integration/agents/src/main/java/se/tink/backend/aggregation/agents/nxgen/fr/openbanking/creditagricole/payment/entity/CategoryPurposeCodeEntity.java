package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CategoryPurposeCodeEntity {
    CASH("CASH"),
    DVPM("DVPM");

    private String value;

    CategoryPurposeCodeEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static CategoryPurposeCodeEntity fromValue(String text) {
        for (CategoryPurposeCodeEntity b : CategoryPurposeCodeEntity.values()) {
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
