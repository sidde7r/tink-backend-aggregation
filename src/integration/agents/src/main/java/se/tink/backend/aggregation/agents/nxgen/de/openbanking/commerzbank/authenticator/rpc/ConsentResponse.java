package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentId;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    private String consentStatus;

    public String getConsentId() {
        return consentId;
    }

    public LinksEntity getLinksEntity() {
        return linksEntity;
    }

    public String getConsentStatus() {
        return consentStatus;
    }
}
