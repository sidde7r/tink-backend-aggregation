package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdPollResponse {
    private String state;

    public String getState() {
        return state;
    }
}
