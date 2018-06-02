package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorDetailsEntity {
    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
