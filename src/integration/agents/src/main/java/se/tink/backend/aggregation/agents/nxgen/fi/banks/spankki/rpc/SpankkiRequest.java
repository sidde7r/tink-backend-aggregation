package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.entities.ClientInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SpankkiRequest {
    private String requestToken;
    private String sessionId;
    private String deviceId;
    private ClientInfoEntity clientInfo;

    public SpankkiRequest() {
        this.clientInfo = new ClientInfoEntity();
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setRequestToken(String requestToken) {
        this.requestToken = requestToken;
    }

    public SpankkiRequest setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public void setClientInfo(ClientInfoEntity clientInfo) {
        this.clientInfo = clientInfo;
    }
}
