package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.FinalizeAuthorizationLinksEntity;

public class FinalizeAuthorizationResponse {

    private String scaStatus;

    @JsonProperty("_links")
    private FinalizeAuthorizationLinksEntity links;

    public String getScaStatus() {
        return scaStatus;
    }
}
