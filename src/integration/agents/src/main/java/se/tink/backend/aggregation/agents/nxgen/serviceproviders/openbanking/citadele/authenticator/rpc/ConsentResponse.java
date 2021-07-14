package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.entities.ConsentBaseLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentStatus;
    private String consentId;
    private String scaMethods;
    private String chosenScaMethod;
    private String challengeData;

    @JsonProperty("_links")
    private ConsentBaseLinksEntity links;

    public String getConsentId() {
        return consentId;
    }

    public ConsentBaseLinksEntity getLinks() {
        return links;
    }

    @JsonIgnore
    public String getConsentStatus() {
        return consentStatus;
    }
}
