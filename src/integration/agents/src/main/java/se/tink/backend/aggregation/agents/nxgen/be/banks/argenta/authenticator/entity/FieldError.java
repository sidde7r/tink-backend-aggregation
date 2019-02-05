package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FieldError {
    String field;
    String code;
    String message;

    public String getMessage() {
        return message;
    }
}
