package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AssertionType {
    AUTHENTICATE("authenticate"),
    REGISTER("register");

    private final String value;

    AssertionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
