package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    @JsonProperty("consent_uri")
    private String consentUri;

    @JsonProperty("language")
    private String language;

    public String getConsentUri() {
        return consentUri;
    }

    public String getLanguage() {
        return language;
    }
}
