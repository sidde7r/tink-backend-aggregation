package se.tink.backend.aggregation.agents.banks.lansforsakringar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.utils.CookieContainer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Session extends CookieContainer {
    private String token;
    private String ticket;
    private String loginName;
    private String loginSsn;
    private String userSession;

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

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginSsn() {
        return loginSsn;
    }

    public void setLoginSsn(String loginSsn) {
        this.loginSsn = loginSsn;
    }

    public String getUserSession() {
        return userSession;
    }

    public void setUserSession(String userSession) {
        this.userSession = userSession;
    }
}
