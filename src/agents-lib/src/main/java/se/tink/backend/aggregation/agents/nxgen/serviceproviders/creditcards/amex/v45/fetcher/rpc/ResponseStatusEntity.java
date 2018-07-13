package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResponseStatusEntity {
    private int status;
    private String message;
    private String messageType;
    private String statusCode;

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getStatusCode() {
        return statusCode;
    }
}
