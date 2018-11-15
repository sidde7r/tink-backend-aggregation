package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities.EBankingUserIdEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationProcessRequest {
    private EBankingUserIdEntity ebankingUserId;
    private String distributorId;
    private String authenticationMeanId;
}
