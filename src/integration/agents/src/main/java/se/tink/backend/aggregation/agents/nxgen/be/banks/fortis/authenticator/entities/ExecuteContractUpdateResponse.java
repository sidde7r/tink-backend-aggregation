package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExecuteContractUpdateResponse {
    private boolean value;
    private BusinessMessageBulk businessMessageBulk;

    public BusinessMessageBulk getBusinessMessageBulk() {
        return businessMessageBulk;
    }
}
