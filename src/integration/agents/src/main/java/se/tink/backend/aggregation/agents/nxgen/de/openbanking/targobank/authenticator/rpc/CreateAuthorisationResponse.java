package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities.CreateAuthorisationLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateAuthorisationResponse {
    private String scaStatus;
    private String authorisationId;

    @JsonProperty("_links")
    private CreateAuthorisationLinksEntity links;

    public CreateAuthorisationLinksEntity getLinks() {
        return links;
    }
}
