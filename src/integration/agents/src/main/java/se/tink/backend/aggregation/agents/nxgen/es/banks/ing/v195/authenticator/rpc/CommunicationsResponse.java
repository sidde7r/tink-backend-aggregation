package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CommunicationsResponse {

    private String response;

    public String getResponse() {
        return response;
    }
}
