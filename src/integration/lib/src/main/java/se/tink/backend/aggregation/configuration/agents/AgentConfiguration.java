package se.tink.backend.aggregation.configuration.agents;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgentConfiguration<T> implements ClientConfiguration {

    private T providerSpecificConfiguration;
    @JsonProperty private String redirectUrl;

    public T getProviderSpecificConfiguration() {
        return providerSpecificConfiguration;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public static class Builder<T> {
        private T providerSpecificConfiguration;
        private String redirectUrl;

        public Builder() {}

        public Builder setProviderSpecificConfiguration(T providerSpecificConfiguration) {
            this.providerSpecificConfiguration = providerSpecificConfiguration;
            return this;
        }

        public Builder setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }

        public AgentConfiguration<T> build() {
            AgentConfiguration<T> agentConfiguration = new AgentConfiguration<>();
            agentConfiguration.providerSpecificConfiguration = this.providerSpecificConfiguration;
            agentConfiguration.redirectUrl = this.redirectUrl;
            return agentConfiguration;
        }
    }
}
