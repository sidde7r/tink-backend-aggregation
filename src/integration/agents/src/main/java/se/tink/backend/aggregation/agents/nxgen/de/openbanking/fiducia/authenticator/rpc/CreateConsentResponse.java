package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateConsentResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private String consentId;
    private String consentStatus;

    public String getConsentId() {
        return consentId;
    }
}
