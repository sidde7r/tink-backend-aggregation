package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.AuthorizationResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.GroupHeader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeResponse extends NordeaBaseResponse {

    @JsonProperty("group_header")
    private GroupHeader groupHeader;

    private AuthorizationResponseEntity response;

    public AuthorizationResponseEntity getResponse() {
        return response;
    }

    public GroupHeader getGroupHeader() {
        return groupHeader;
    }
}
