package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.entity.ConsentLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentStatus;
    private String consentId;

    @JsonProperty("_links")
    private ConsentLinksEntity links;

    public String getConsentId() {
        return consentId;
    }

    public ConsentLinksEntity getLinks() {
        return links;
    }
}
