package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdAuthenticationInEntity {
    private String userId;

    public InitBankIdAuthenticationInEntity(String userId) {
        this.userId = userId;
    }
}
