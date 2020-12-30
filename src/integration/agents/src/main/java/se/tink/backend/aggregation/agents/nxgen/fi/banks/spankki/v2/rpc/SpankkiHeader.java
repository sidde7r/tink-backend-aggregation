package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.entities.ClientInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class SpankkiHeader {
    @JsonProperty private String sessionId;
    @JsonProperty private String deviceId;
    @JsonProperty private final ClientInfoEntity clientInfo;

    public SpankkiHeader() {
        this.clientInfo = new ClientInfoEntity();
    }

    @JsonIgnore
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @JsonIgnore
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
