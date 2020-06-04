package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PutRestSessionResponse {

    private String ticket;

    private Long timeoutInSeconds;

    private String rememberMeToken;

    private Integer resultCode;

    private String resultMessage;

    private Integer nextValMethod;

    public String getTicket() {
        return ticket;
    }

    public Integer getNextValMethod() {
        return nextValMethod;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Long getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public String getRememberMeToken() {
        return rememberMeToken;
    }
}
