package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.entities.IdEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstallResponse {
    @JsonProperty("Id")
    private IdEntity id;

    @JsonProperty("Token")
    private TokenEntity token;

    @JsonProperty("ServerPublicKey")
    private ServerPublicKeyEntity serverPublicKey;

    public IdEntity getId() {
        if (id == null) {
            throw new IllegalStateException("Could not get Id.");
        }
        return id;
    }

    public TokenEntity getToken() {
        return token;
    }
}
