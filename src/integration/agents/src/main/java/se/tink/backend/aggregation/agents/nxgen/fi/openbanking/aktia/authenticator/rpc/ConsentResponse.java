package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {
    @JsonProperty("_links")
    public LinksEntity links;
    private String consentStatus;
    private String consentId;

    public LinksEntity getLinks() {
        return links;
    }

    public String getConsentId() {
        return consentId;
    }
}
