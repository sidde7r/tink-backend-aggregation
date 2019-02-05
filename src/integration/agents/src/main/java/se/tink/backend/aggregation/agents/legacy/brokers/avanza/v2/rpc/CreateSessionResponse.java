package se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSessionResponse {
    private String authenticationSession;
    private String pushSubscriptionId;
    private String customerId;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPushSubscriptionId() {
        return pushSubscriptionId;
    }

    public void setPushSubscriptionId(String pushSubscriptionId) {
        this.pushSubscriptionId = pushSubscriptionId;
    }

    public String getAuthenticationSession() {
        return authenticationSession;
    }

    public void setAuthenticationSession(String authenticationSession) {
        this.authenticationSession = authenticationSession;
    }
}
