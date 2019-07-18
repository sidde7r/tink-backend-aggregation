package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    @JsonProperty("consentId")
    private String consentId;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonProperty("consentStatus")
    private String consentStatus;

    public String getConsentId() {
        return consentId;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getScaRedirect() {
        return links.getScaRedirect().getHref();
    }

    public String getConsentStatus() {
        return consentStatus;
    }
}
