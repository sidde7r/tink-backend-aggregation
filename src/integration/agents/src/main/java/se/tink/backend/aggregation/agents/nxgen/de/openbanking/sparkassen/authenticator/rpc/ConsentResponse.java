package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {
    private String consentStatus;
    private String consentId;

    @JsonProperty("_links")
    private LinksEntity links;

    private String psuMessage;

    public String getConsentId() {
        return consentId;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
