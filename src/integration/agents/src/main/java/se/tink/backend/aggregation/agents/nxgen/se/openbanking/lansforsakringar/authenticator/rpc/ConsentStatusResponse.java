package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {

    private static final String VALID = "valid";

    private String consentStatus;

    public boolean isConsentValid() {
        return VALID.equalsIgnoreCase(consentStatus);
    }
}
