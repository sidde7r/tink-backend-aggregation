package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionEntity {
    @JsonProperty("bharosaSessionId")
    private String sessionId;

    public Optional<String> getSessionId() {
        return Optional.ofNullable(sessionId);
    }
}
