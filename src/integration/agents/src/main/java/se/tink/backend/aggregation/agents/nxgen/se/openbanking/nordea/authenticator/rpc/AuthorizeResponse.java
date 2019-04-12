package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.ResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeResponse {

    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    private ResponseEntity response;

    public ResponseEntity getResponse() {
        return response;
    }
}
