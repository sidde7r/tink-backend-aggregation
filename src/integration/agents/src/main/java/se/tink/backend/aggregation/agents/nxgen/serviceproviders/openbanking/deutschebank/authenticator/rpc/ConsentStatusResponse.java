package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {

    @JsonProperty
    private String consentStatus;

    @JsonIgnore
    public String getConsentStatus() {
        return consentStatus;
    }
}
