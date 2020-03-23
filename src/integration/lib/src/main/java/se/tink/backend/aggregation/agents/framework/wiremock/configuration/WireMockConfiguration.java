package se.tink.backend.aggregation.agents.framework.wiremock.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.util.Collections;
import java.util.Map;

public final class WireMockConfiguration {

    private final String serverUrl;
    private final String agentConfigurationPath;
    private final Map<String, String> callbackData;
    private final Module agentModule;

    public WireMockConfiguration(String serverUrl) {
        this.serverUrl = serverUrl;
        this.agentConfigurationPath = null;
        this.callbackData = Collections.emptyMap();
        this.agentModule = new AbstractModule() {}; // Empty module
    }

    public WireMockConfiguration(
            String serverUrl,
            String agentConfigurationPath,
            Map<String, String> callbackData,
            Module agentModule) {
        this.serverUrl = serverUrl;
        this.agentConfigurationPath = agentConfigurationPath;
        this.callbackData = callbackData;
        this.agentModule = agentModule;
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

    public Module getAgentModule() {
        return agentModule;
    }
}
