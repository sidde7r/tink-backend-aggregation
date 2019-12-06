package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.AuthorizationResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeResponse extends NordeaBaseResponse {

    private AuthorizationResponseEntity response;

    public AuthorizationResponseEntity getResponse() {
        return response;
    }
}
