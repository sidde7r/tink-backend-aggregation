package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetConsentResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private String consentId;
    private String consentStatus;

    public String getConsentId() {
        return consentId;
    }
}
