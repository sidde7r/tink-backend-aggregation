package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities.ConsentLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PostConsentResponse {

    @JsonProperty("_links")
    private ConsentLinksEntity links;

    private String consentId;
    private String consentStatus;

    public String getConsentId() {
        return consentId;
    }
}
