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

    private WireMockConfiguration(
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

    public static Builder builder(String serverUrl) {
        return new Builder(serverUrl);
    }

    public static class Builder {

        private final String serverUrl;
        private String configurationPath;
        private Map<String, String> callbackData;
        private Module agentModule;

        private Builder(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public Builder setConfigurationPath(String configurationPath) {
            this.configurationPath = configurationPath;
            return this;
        }

        public Builder setCallbackData(Map<String, String> callbackData) {
            this.callbackData = callbackData;
            return this;
        }

        public Builder setAgentModule(Module agentModule) {
            this.agentModule = agentModule;
            return this;
        }

        public WireMockConfiguration build() {

            if (callbackData == null) {
                callbackData = Collections.emptyMap();
            }

            if (agentModule == null) {
                agentModule = new AbstractModule() {}; // Empty Module
            }

            return new WireMockConfiguration(
                    serverUrl, configurationPath, callbackData, agentModule);
        }
    }
}
