package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementListEntity {
    private AgreementEntity agreement;

    public AgreementEntity getAgreement() {
        return agreement;
    }
}
