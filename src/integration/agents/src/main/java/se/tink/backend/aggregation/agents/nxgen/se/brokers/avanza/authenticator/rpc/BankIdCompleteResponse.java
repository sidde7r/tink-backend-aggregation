package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdCompleteResponse {
    private String securityToken;
    private String authenticationSession;
    private String pushSubscriptionId;
    private String customerId;
    private boolean registrationComplete;

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public BankIdCompleteResponse withSecurityToken(String securityToken) {
        this.securityToken = securityToken;
        return this;
    }

    public String getAuthenticationSession() {
        return authenticationSession;
    }

    public String getPushSubscriptionId() {
        return pushSubscriptionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public boolean isRegistrationComplete() {
        return registrationComplete;
    }
}
