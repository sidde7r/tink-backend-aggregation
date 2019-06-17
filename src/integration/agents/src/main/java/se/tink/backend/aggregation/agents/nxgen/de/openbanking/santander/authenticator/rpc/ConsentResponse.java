package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.entities.ConsentLinks;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentStatus;
    private String consentId;

    @JsonProperty("_links")
    private ConsentLinks links;

    public ConsentResponse() {}

    public ConsentResponse(final String consentStatus, final String consentId) {
        this.consentStatus = consentStatus;
        this.consentId = consentId;
    }

    public ConsentLinks getLinks() {
        return links;
    }

    public String getConsentId() {
        return consentId;
    }
}
