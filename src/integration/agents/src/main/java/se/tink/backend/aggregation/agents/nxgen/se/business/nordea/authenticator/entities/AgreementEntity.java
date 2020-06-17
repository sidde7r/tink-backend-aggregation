package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementEntity {
    private String id;
    private String agreementId;
    private String agreementHolderID;
    private String agreementHolderName;

    public String getId() {
        return id;
    }

    public String getAgreementHolderID() {
        return agreementHolderID;
    }

    public String getAgreementHolderName() {
        return agreementHolderName;
    }
}
