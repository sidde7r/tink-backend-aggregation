package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities.InitBankIdAuthenticationInEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdRequest {
    public InitBankIdAuthenticationInEntity initBankIdAuthenticationIn;

    public InitBankIdRequest(String ssn) {
        initBankIdAuthenticationIn = new InitBankIdAuthenticationInEntity(ssn);
    }
}
