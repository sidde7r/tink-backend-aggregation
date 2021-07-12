package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private String consentId;
    private String consentStatus;

    protected boolean isConsentValid(String consentStatus) {
        return "received".equalsIgnoreCase(consentStatus)
                || "valid".equalsIgnoreCase(consentStatus);
    }
}
