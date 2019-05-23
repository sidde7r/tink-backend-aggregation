package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.entities.UserPersonEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.IdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.TokenEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateSessionUserResponse {
    @JsonProperty("Id")
    private IdEntity id;

    @JsonProperty("Token")
    private TokenEntity token;

    @JsonProperty("UserPerson")
    private UserPersonEntity userPerson;

    public IdEntity getId() {
        return Optional.ofNullable(id)
                .orElseThrow(() -> new IllegalStateException("Could not get Id."));
    }

    public TokenEntity getToken() {
        return token;
    }

    public UserPersonEntity getUserPerson() {
        return userPerson;
    }
}
