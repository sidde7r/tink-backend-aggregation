package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String scaStatus;

    private String authorisationId;

    private String consentId;

    @JsonProperty("_links")
    private LinksEntity links;

    private String consentStatus;

    public String getScaStatus() {
        return scaStatus;
    }

    public String getAuthorisationId() {
        return authorisationId;
    }

    public String getConsentId() {
        return consentId;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getConsentStatus() {
        return consentStatus;
    }
}
