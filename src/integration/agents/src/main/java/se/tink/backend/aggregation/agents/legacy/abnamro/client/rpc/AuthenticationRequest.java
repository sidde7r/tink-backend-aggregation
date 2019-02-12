package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import io.protostuff.Tag;

public class AuthenticationRequest implements AuthenticatedRequest {

    @Tag(1)
    private String bcNumber;

    @Tag(2)
    private String sessionToken;

    @Tag(3)
    private String locale;

    public String getBcNumber() {
        return bcNumber;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setBcNumber(String bcNumber) {
        this.bcNumber = bcNumber;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
