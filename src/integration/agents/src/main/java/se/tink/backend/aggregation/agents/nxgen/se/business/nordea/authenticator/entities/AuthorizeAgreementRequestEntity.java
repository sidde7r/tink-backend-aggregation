package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeAgreementRequestEntity {
    private String agreement;

    public AuthorizeAgreementRequestEntity(String agreement) {
        this.agreement = agreement;
    }
}
