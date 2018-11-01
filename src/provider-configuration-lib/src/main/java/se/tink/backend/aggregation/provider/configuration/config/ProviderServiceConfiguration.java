package se.tink.backend.aggregation.provider.configuration.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.ServiceAuthenticationConfiguration;
import se.tink.libraries.metrics.PrometheusConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderServiceConfiguration extends Configuration {
    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @JsonProperty
    private PrometheusConfiguration prometheus = () -> 0;

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public PrometheusConfiguration getPrometheus() {
        return prometheus;
    }
}
