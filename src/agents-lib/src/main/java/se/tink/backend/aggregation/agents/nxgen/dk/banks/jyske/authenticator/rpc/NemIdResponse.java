package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdResponse {
    private String data;

    public String getData() {
        return data;
    }
}
