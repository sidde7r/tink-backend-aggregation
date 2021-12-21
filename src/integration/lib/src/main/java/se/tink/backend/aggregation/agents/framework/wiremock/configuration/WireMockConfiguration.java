package se.tink.backend.aggregation.agents.framework.wiremock.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.util.Map;

public final class WireMockConfiguration {

    private final String agentConfigurationPath;
    private final Map<String, String> callbackData;
    private final Module agentModule;

    private WireMockConfiguration(
            String agentConfigurationPath, Map<String, String> callbackData, Module agentModule) {
        this.agentConfigurationPath = agentConfigurationPath;
        this.callbackData = callbackData;
        this.agentModule = agentModule;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String configurationPath;
        private Map<String, String> callbackData;
        private Module agentModule;

        private Builder() {}

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
            if (agentModule == null) {
                agentModule = new AbstractModule() {}; // Empty Module
            }
            return new WireMockConfiguration(configurationPath, callbackData, agentModule);
        }
    }
}
