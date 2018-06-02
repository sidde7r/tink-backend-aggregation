package se.tink.backend.rpc.abnamro;

import io.protostuff.Tag;

public class SubscriptionActivationRequest implements AuthenticatedRequest {

    @Tag(1)
    private String bcNumber;

    @Tag(2)
    private String sessionToken;

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
}
