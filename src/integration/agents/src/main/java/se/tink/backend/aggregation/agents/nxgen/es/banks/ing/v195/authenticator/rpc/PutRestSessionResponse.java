package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PutRestSessionResponse {

    private String ticket;

    private Long timeoutInSeconds;

    private String rememberMeToken;

    public String getTicket() {
        return ticket;
    }

    public Long getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public String getRememberMeToken() {
        return rememberMeToken;
    }

}
