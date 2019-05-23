package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.IdEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddOAuthClientIdResponse {
    @JsonProperty("Id")
    private IdEntity id;

    public IdEntity getId() {
        return Optional.ofNullable(id)
                .orElseThrow(() -> new IllegalStateException("Could not get Id."));
    }
}
