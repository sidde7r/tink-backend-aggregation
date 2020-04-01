package se.tink.backend.aggregation.configuration.agentsservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class TestConfiguration {

    @JsonProperty private boolean censorSensitiveHeaders = true;
    @JsonProperty private boolean debugOutput;

    @JsonProperty private boolean mockServer = false;
    @JsonProperty private String mockURL;

    @JsonIgnore private int mockServerPort;

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

    public void setMockServerPort(int port) {
        this.mockServerPort = port;
    }

    public int getMockServerPort() {
        return mockServerPort;
    }
}
