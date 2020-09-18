package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateSignResponse {
    private String id;
    private String status;
    private String autostartToken;

    public String getStatus() {
        return status;
    }
}
