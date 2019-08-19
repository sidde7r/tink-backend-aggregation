package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {

    private String consentStatus;

    public String getConsentStatus() {
        return consentStatus;
    }
}
