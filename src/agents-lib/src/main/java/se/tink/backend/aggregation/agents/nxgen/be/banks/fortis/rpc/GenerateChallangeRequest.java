package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenerateChallangeRequest {
    private String distributorId;
    private String authenticationProcessId;

    public GenerateChallangeRequest(String distributorId, String authenticationProcessId) {
        this.distributorId = distributorId;
        this.authenticationProcessId = authenticationProcessId;
    }
}
