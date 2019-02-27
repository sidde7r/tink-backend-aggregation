package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeepAliveResponse {
    private String statusCode;
    private String description;
    private String alias;
}
