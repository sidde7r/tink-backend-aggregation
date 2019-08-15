package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentId;

    @JsonProperty("_links")
    private LinksEntity links;

    private String consentStatus;

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
