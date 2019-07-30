package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ConsentStatusStates;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {

    private String consentStatus;
    private String scaStatus;

    public boolean isValidConsent() {

        return ConsentStatusStates.VALID.equalsIgnoreCase(consentStatus);
    }
}
