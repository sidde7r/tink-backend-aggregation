package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EBankingUsersRequest {
    private String authenticationFactorId;
    private String distributorId;
    private String smid;

    public EBankingUsersRequest(String authenticationFactorId, String distributorId, String smid) {
        this.authenticationFactorId = authenticationFactorId;
        this.distributorId = distributorId;
        this.smid = smid;
    }

    public String getDistributorId() {
        return distributorId;
    }
}
