package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EBankingUserId {
    private String smid;
    private String mobileStatus;
    private String agreementId;
    private String personId;

    public String getSmid() {
        return smid;
    }

    public String getMobileStatus() {
        return mobileStatus;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public String getPersonId() {
        return personId;
    }
}
