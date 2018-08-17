package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MessageEntity {
    private String field;
    private String message;
    private String errorCode;

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
