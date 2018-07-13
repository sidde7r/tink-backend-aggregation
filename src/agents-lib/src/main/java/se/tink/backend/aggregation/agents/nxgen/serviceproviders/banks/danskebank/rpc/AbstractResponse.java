package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractResponse {
    private String eupToken;
    private int responseCode;
    private String responseMessage;
    private String sessionId;
    private String userId;

    public String getEupToken() {
        return eupToken;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }
}
