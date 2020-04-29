package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities.InitBankIdAuthenticationOutEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.rpc.NordeaSEResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdResponse extends NordeaSEResponse {
    private InitBankIdAuthenticationOutEntity initBankIdAuthenticationOut;

    public String getReference() {
        return initBankIdAuthenticationOut.getBankIdAuthenticationRequestToken();
    }

    public String getSecurityToken() {
        return initBankIdAuthenticationOut.getSecurityToken();
    }
}
