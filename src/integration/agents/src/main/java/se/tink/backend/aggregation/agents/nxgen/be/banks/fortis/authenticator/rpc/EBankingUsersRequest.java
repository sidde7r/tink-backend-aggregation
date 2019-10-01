package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EBankingUsersRequest {
    private String authenticationFactorId;
    private String distributorId;
    private String smid;
    private String cardFrameId;

    public EBankingUsersRequest(
            String authenticationFactorId, String distributorId, String smid, String cardFrameId) {
        this.authenticationFactorId = authenticationFactorId;
        this.distributorId = distributorId;
        this.smid = smid;
        this.cardFrameId = cardFrameId;
    }

    public String getDistributorId() {
        return distributorId;
    }
}
