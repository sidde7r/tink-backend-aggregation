package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeResponse {

    @JsonProperty("CONSENT_FORM_VERIFIER")
    private String consentFormVerifier;

    private String code;

    private String state;

    public String getConsentFormVerifier() {
        return consentFormVerifier;
    }

    public String getCode() {
        return code;
    }

    public String getState() {
        return state;
    }
}
