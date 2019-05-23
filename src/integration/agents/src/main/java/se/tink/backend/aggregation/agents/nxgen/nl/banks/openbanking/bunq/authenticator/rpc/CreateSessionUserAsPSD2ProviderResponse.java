package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities.UserApiKeyEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.IdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.TokenEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateSessionUserAsPSD2ProviderResponse {
    @JsonProperty("Id")
    private IdEntity id;

    @JsonProperty("Token")
    private TokenEntity token;

    @JsonProperty("UserApiKey")
    private UserApiKeyEntity userApiKey;

    public UserApiKeyEntity getUserApiKey() {
        return userApiKey;
    }

    public TokenEntity getToken() {
        return token;
    }
}
