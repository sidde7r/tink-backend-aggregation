package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareContractUpdateRequest {
    private String tcFlag;
    private String password;

    public PrepareContractUpdateRequest(String tcFlag, String password) {
        this.tcFlag = tcFlag;
        this.password = password;
    }
}
