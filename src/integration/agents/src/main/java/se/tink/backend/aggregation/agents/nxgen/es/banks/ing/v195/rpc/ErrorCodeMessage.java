package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorCodeMessage {
    private String message;
    private int errorCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
