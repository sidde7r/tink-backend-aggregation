package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {
    @JsonProperty("_links")
    private LinksEntity links;

    private String consentId;
    private String consentStatus;

    public LinksEntity getLinks() {
        return links;
    }

    public String getConsentId() {
        return consentId;
    }

    public String getConsentStatus() {
        return consentStatus;
    }
}
