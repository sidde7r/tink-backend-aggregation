package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class TestConfiguration {

    @JsonProperty private boolean censorSensitiveHeaders = true; // Default
    @JsonProperty private boolean debugOutput;

    @JsonProperty private boolean mockServer = false;
    @JsonProperty private String mockURL;

    public boolean isDebugOutputEnabled() {
        return debugOutput;
    }

    public boolean isCensorSensitiveHeadersEnabled() {
        return censorSensitiveHeaders;
    }

    public boolean isMockServer() {
        return mockServer;
    }

    public String getMockURL() {
        return mockURL;
    }
}
