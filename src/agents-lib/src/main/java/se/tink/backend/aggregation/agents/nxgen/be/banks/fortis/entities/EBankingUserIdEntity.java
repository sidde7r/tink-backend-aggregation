package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EBankingUserIdEntity {
    private String personId;
    private String smid;
    private String agreementId;
    private String mobileStatus;

    public EBankingUserIdEntity(String personId, String smid, String agreementId, String mobileStatus) {
        this.personId = personId;
        this.smid = smid;
        this.agreementId = agreementId;
        this.mobileStatus = mobileStatus;
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

    public String getMobileStatus() {
        return mobileStatus;
    }
}
