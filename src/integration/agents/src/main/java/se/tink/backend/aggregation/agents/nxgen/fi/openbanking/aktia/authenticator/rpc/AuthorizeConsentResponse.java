package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.entities.AuthorisationLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeConsentResponse {

    @JsonProperty("_links")
    private AuthorisationLinksEntity links;

    private String authorisationId;
    private String scaStatus;

    public AuthorisationLinksEntity getLinks() {
        return links;
    }
}
