package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.ConsentBaseWithoutLinks;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentBaseResponseWithoutHref {

    private String consentStatus;
    private String consentId;

    @JsonProperty("_links")
    private ConsentBaseWithoutLinks links;

    public ConsentBaseResponseWithoutHref() {}

    public ConsentBaseResponseWithoutHref(final String consentStatus, final String consentId) {
        this.consentStatus = consentStatus;
        this.consentId = consentId;
    }

    public String getConsentId() {
        return consentId;
    }

    public ConsentBaseWithoutLinks getLinks() {
        return links;
    }
}
