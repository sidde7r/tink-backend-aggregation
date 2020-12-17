package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MethodType {
    OTP("otp"),
    PIN("pin");

    private final String value;

    MethodType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
