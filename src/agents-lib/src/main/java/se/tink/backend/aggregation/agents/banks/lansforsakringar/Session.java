package se.tink.backend.aggregation.agents.banks.lansforsakringar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.utils.CookieContainer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Session extends CookieContainer {
    private String token;
    private String ticket;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getTicket() {
        return ticket;
    }
}
