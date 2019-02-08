package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentsServiceConfigurationWrapper extends Configuration {
    @JsonProperty
    private AgentsServiceConfiguration agentsServiceConfiguration =
            new AgentsServiceConfiguration();

    public AgentsServiceConfiguration getAgentsServiceConfiguration() {
        return agentsServiceConfiguration;
    }
}
