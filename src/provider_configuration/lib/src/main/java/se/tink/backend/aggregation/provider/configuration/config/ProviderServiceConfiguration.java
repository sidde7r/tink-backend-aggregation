package se.tink.backend.aggregation.provider.configuration.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import se.tink.libraries.repository.config.DatabaseConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderServiceConfiguration extends Configuration {
    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @JsonProperty
    private PrometheusConfiguration prometheus = new PrometheusConfiguration();

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public PrometheusConfiguration getPrometheus() {
        return prometheus;
    }
}
