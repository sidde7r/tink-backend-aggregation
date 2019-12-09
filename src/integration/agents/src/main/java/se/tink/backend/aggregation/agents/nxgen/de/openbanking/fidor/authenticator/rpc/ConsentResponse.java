package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {
    private String consentId;
    private String consentStatus;

    private ScaMethodEntity chosenScaMethod;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getConsentId() {
        return consentId;
    }

    public String getAuthenticationMethodId() {
        return chosenScaMethod.getAuthenticationMethodId();
    }

    public String getAuthorizationLink() {
        return links.getAutorizationLink();
    }
}
