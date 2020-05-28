package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    private int code;
    private String message;
    private Object params;
    private String type;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
