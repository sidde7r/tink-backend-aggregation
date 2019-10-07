package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExecuteContractUpdateRequest {
    private SignatureEntity signature;

    public ExecuteContractUpdateRequest(SignatureEntity signature) {
        this.signature = signature;
    }
}
