package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities.ConsentBaseLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRedirectResponse {

    @JsonProperty private String consentStatus;
    @JsonProperty private String consentId;

    @JsonProperty("_links")
    private ConsentBaseLinksEntity links;

    @JsonIgnore
    public String getConsentId() {
        return consentId;
    }

    @JsonIgnore
    public ConsentBaseLinksEntity getLinks() {
        return links;
    }

    @JsonIgnore
    public String getConsentStatus() {
        return consentStatus;
    }
}
