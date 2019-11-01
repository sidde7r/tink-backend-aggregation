package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.entity.ConsentLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentId;
    private String consentStatus;

    @JsonProperty("_links")
    private ConsentLinksEntity links;

    public String getConsentId() {
        return consentId;
    }
}
