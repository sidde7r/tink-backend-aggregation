package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EncapResponse extends SpankkiResponse {
    @JsonProperty private Boolean pendingPerform;
    @JsonProperty private String sessionKey;
    @JsonProperty private Boolean isFimUser;

    @JsonIgnore
    public Boolean isPendingPerform() {
        return pendingPerform;
    }
}
