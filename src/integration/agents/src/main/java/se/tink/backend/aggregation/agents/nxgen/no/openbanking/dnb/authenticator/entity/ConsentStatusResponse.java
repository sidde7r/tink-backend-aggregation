package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {
    private static final String VALID = "valid";
    private String consentStatus;

    public boolean isValid() {
        return VALID.equalsIgnoreCase(consentStatus);
    }
}
