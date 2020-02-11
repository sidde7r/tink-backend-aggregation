package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentStatus;

    public ConsentStatus getConsentStatus() {
        return ConsentStatus.valueOf(consentStatus.toUpperCase());
    }
}
