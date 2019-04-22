package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentBaseResponse {

    private String consentStatus;
    private String consentId;

    public ConsentBaseResponse() {}

    public ConsentBaseResponse(String consentStatus, String consentId) {
        this.consentStatus = consentStatus;
        this.consentId = consentId;
    }

    public String getConsentId() {
        return consentId;
    }
}
