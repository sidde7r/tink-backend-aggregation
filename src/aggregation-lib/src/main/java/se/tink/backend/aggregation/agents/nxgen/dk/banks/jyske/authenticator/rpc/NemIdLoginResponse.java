package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdLoginResponse {

    private boolean ok;

    public boolean isOk() {
        return ok;
    }
}
