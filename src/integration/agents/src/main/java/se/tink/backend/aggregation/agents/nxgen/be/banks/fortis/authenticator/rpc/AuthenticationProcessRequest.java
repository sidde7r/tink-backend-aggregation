package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EBankingUserId;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationProcessRequest {
    private EBankingUserId ebankingUserId;
    private String distributorId;
    private String authenticationMeanId;

    public AuthenticationProcessRequest(
            EBankingUserId ebankingUserId, String distributorId, String authenticationMeanId) {
        this.ebankingUserId = ebankingUserId;
        this.distributorId = distributorId;
        this.authenticationMeanId = authenticationMeanId;
    }

    public EBankingUserId getEbankingUserId() {
        return ebankingUserId;
    }
}
