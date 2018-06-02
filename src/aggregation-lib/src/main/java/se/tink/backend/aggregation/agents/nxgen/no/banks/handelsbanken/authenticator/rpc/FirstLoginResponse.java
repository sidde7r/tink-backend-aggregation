package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FirstLoginResponse {
    private String protocolVersion;
    private String accessToken;
    private String sessionId;

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getSessionId() {
        return sessionId;
    }
}
