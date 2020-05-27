package se.tink.backend.aggregation.configuration.agents;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgentConfiguration<T> implements ClientConfiguration {

    private T clientConfiguration;
    @JsonProperty private String redirectUrl;

    public T getClientConfiguration() {
        return clientConfiguration;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public static class Builder<T> {
        private T clientConfiguration;
        private String redirectUrl;

        public Builder() {}

        public Builder setClientConfiguration(T clientConfiguration) {
            this.clientConfiguration = clientConfiguration;
            return this;
        }

        public Builder setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }

        public AgentConfiguration<T> build() {
            AgentConfiguration<T> agentConfiguration = new AgentConfiguration<>();
            agentConfiguration.clientConfiguration = this.clientConfiguration;
            agentConfiguration.redirectUrl = this.redirectUrl;
            return agentConfiguration;
        }
    }
}
