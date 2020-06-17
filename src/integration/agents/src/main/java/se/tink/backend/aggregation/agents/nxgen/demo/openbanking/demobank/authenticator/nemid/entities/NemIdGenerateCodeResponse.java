package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdGenerateCodeResponse {
    private String pollUrl;
    private String clientPollTimeout;
    private String token;
    private String securityWordEnabled;
    private String challengeExpiry;

    public String getPollUrl() {
        return pollUrl;
    }

    public String getClientPollTimeout() {
        return clientPollTimeout;
    }

    public String getToken() {
        return token;
    }

    public String getSecurityWordEnabled() {
        return securityWordEnabled;
    }

    public String getChallengeExpiry() {
        return challengeExpiry;
    }
}
