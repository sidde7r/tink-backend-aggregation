package se.tink.backend.aggregation.register.nl.bunq.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.IdEntity;

public class RegisterCallbackResponse {
    @JsonProperty("Id")
    private IdEntity id;

    public IdEntity getId() {
        if (id == null) {
            throw new IllegalStateException("Could not get Id.");
        }
        return id;
    }
}
