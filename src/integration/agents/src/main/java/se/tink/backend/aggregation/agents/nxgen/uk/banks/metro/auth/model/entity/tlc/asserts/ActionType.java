package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ActionType {
    REGISTRATION("registration"),
    AUTHENTICATION("authentication");

    private final String value;

    ActionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
