package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionEntity {
    @JsonProperty("SessionId")
    private String sessionId;

    @JsonProperty("ClientSessionTimeToLive")
    private int clientSessionTimeToLive;

    @JsonProperty("HeartbeatInterval")
    private int heartbeatInterval;

    public String getSessionId() {
        return sessionId;
    }

    public int getClientSessionTimeToLive() {
        return clientSessionTimeToLive;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }
}
