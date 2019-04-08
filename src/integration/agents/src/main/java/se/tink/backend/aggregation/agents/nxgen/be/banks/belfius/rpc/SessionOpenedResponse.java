package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.SessionOpenedResponseDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonDeserialize(using = SessionOpenedResponseDeserializer.class)
public class SessionOpenedResponse extends ResponseEntity {

    private final String sessionId;
    private final String machineIdentifier;
    private final int heartbeatInterval;

    public SessionOpenedResponse(
            String sessionId, String machineIdentifier, int heartbeatInterval) {
        this.sessionId = sessionId;
        this.machineIdentifier = machineIdentifier;
        this.heartbeatInterval = heartbeatInterval;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public String getMachineIdentifier() {
        return this.machineIdentifier;
    }

    public int getHeartbeatInterval() {
        return this.heartbeatInterval;
    }
}
