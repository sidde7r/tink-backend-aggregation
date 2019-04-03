package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.entity.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentStatus;
    private String consentId;
    private String scaMethods;
    private String chosenScaMethod;
    private String challengeData;
    private String message;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getConsentStatus() {
        return consentStatus;
    }

    public String getConsentId() {
        return consentId;
    }
}
