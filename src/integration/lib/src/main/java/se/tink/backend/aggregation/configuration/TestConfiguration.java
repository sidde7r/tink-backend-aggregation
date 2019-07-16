package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class TestConfiguration {

    @JsonProperty private boolean debugOutput;

    public boolean isDebugOutputEnabled() {
        return debugOutput;
    }
}
