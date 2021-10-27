package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities.UserApiKeyEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.IdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateSessionUserAsPSD2ProviderResponse {

    @JsonProperty("Id")
    private IdEntity id;

    @Getter
    @JsonProperty("Token")
    private TokenEntity token;

    @Getter
    @JsonProperty("UserApiKey")
    private UserApiKeyEntity userApiKey;
}
