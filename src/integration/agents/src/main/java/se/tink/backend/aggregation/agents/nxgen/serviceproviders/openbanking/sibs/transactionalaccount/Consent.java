package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Consent {

    private String consentId;
    private String consentCreated;

    public Consent() {}

    public Consent(String consentId, String consentCreated) {
        this.consentId = consentId;
        this.consentCreated = consentCreated;
    }

    public String getConsentCreated() {
        return consentCreated;
    }

    public String getConsentId() {
        return consentId;
    }
}
