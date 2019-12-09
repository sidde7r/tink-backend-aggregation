package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.ConsentBaseLinks;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentBaseResponse {

    private String consentStatus;
    private String consentId;

    @JsonProperty("_links")
    private ConsentBaseLinks links;

    public ConsentBaseResponse() {}

    public ConsentBaseResponse(final String consentStatus, final String consentId) {
        this.consentStatus = consentStatus;
        this.consentId = consentId;
    }

    public String getConsentId() {
        return consentId;
    }

    public ConsentBaseLinks getLinks() {
        return links;
    }
}
