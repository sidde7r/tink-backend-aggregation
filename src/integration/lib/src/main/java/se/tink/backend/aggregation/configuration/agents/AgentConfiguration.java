package se.tink.backend.aggregation.configuration.agents;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgentConfiguration<T> implements ClientConfiguration {

    private T providerSpecificConfiguration;
    @JsonProperty private String redirectUrl;

    public T getProviderSpecificConfiguration() {
        Preconditions.checkNotNull(
                providerSpecificConfiguration, "Provider specific configuration is missing.");
        return providerSpecificConfiguration;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                "Invalid Configuration: Redirect URL cannot be empty or null");
        return redirectUrl;
    }

    public boolean isProviderSpecificConfigurationNull() {
        return providerSpecificConfiguration == null ? true : false;
    }

    public boolean isRedirectUrlNullOrEmpty() {
        return Strings.emptyToNull(redirectUrl) == null ? true : false;
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
