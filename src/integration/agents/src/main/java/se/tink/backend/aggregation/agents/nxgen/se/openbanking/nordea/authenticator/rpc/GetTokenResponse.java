package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.ResponseTokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class GetTokenResponse extends NordeaBaseResponse {

    private ResponseTokenEntity response;

    public OAuth2Token toTinkToken() {
        return response.toTinkToken();
    }
}
