package se.tink.backend.aggregation.agents.framework.wiremock.configuration;

import java.util.Collections;
import java.util.Map;

public final class WireMockConfiguration {

    private final String serverUrl;
    private final String agentConfigurationPath;
    private final Map<String, String> callbackData;

    public WireMockConfiguration(String serverUrl) {
        this.serverUrl = serverUrl;
        this.agentConfigurationPath = null;
        this.callbackData = Collections.emptyMap();
    }

    public WireMockConfiguration(
            String serverUrl, String agentConfigurationPath, Map<String, String> callbackData) {
        this.serverUrl = serverUrl;
        this.agentConfigurationPath = agentConfigurationPath;
        this.callbackData = callbackData;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getAgentConfigurationPath() {
        return agentConfigurationPath;
    }

    public Map<String, String> getCallbackData() {
        return callbackData;
    }
}
