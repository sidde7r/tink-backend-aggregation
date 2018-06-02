package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceResponse {
    @JsonProperty("Id")
    private IdEntity id;

    public IdEntity getId() {
        return id;
    }
}
