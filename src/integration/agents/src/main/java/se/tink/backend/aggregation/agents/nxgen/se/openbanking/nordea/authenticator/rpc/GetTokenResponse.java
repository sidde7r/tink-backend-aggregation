package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.ResponseTokenEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class GetTokenResponse {

    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    private ResponseTokenEntity response;

    public OAuth2Token toTinkToken() {
        return response.toTinkToken();
    }
}
