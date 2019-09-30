package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentDetailsResponse {

    private Access access;
    private String consentId;
    private String status;
    private String validUntil;

    public Access getAccess() {
        return access;
    }

    public String getConsentId() {
        return consentId;
    }

    public String getStatus() {
        return status;
    }

    public String getValidUntil() {
        return validUntil;
    }
}
