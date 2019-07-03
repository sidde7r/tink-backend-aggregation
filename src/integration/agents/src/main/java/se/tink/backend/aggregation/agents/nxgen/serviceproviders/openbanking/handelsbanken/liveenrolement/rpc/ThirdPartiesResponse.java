package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ThirdPartiesResponse {

    private String clientId;

    public String getClientId() {
        return clientId;
    }
}
