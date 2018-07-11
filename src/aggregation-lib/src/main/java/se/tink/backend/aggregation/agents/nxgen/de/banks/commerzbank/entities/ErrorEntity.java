package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

