package se.tink.backend.aggregation.agents.common.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum CashAccountType {
    CACC("CACC"),
    CARD("CARD");

    private String value;

    CashAccountType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static CashAccountType fromValue(String text) {
        for (CashAccountType b : CashAccountType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }

        log.warn("Unknown account type: {}", text);
        return null;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
