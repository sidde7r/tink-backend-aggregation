package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.entities.IdEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddOAuthClientIdResponse {
    @JsonProperty("Id")
    private IdEntity id;

    public IdEntity getId() {
        if (id == null) {
            throw new IllegalStateException("Could not get Id.");
        }
        return id;
    }
}
