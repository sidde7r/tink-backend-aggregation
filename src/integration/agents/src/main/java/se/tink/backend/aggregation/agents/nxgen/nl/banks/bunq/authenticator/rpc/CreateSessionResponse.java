package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateSessionResponse {
    @JsonProperty("Id")
    private IdEntity id;
    @JsonProperty("Token")
    private TokenEntity token;
    @JsonProperty("UserPerson")
    private UserPersonEntity userPerson;

    public IdEntity getId() {
        return id;
    }

    public TokenEntity getToken() {
        return token;
    }

    public UserPersonEntity getUserPerson() {
        return userPerson;
    }
}
