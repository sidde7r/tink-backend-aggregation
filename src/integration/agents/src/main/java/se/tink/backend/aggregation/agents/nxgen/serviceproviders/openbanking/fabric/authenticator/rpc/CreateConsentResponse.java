package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateConsentResponse {
    private String consentId;
    private String consentStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getConsentId() {
        return consentId;
    }

    public String getAuthorizeUrl() {
        return links.getScaRedirect();
    }
}
