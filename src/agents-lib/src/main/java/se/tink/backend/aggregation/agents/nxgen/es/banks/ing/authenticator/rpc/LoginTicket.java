package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class LoginTicket {

    private String ticket;
    private int timeoutInSeconds;
    private String rememberMeToken;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public String getRememberMeToken() {
        return rememberMeToken;
    }

    public void setRememberMeToken(String rememberMeToken) {
        this.rememberMeToken = rememberMeToken;
    }
}
