package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.rpc;

import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ThirdPartiesResponse {

    @JsonProperty("clientId")
    private String clientId;

    public String getClientId() {
        return clientId;
    }
}
