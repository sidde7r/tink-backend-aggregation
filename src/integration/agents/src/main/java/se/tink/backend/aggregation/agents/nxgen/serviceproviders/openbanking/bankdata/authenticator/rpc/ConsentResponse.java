package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.ConsentLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    @JsonProperty private String consentStatus;
    @JsonProperty private String consentId;

    @JsonProperty("_links")
    private ConsentLinksEntity links;

    public ConsentResponse() {}

    public ConsentResponse(String consentStatus, String consentId) {
        this.consentStatus = consentStatus;
        this.consentId = consentId;
    }

    @JsonIgnore
    public String getConsentId() {
        return consentId;
    }

    @JsonIgnore
    public ConsentLinksEntity getLinks() {
        return links;
    }
}
