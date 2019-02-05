package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EBankingUserIdEntity {
    private String personId;
    private String smid;
    private String agreementId;

    public EBankingUserIdEntity(String personId, String smid, String agreementId) {
        this.personId = personId;
        this.smid = smid;
        this.agreementId = agreementId;
    }

    public String getPersonId() {
        return personId;
    }

    public String getSmid() {
        return smid;
    }

    public String getAgreementId() {
        return agreementId;
    }
}
