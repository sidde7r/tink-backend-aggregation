package se.tink.backend.aggregation.provider.configuration.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrometheusConfiguration implements se.tink.libraries.metrics.PrometheusConfiguration {
    @JsonProperty
    private int port = 0;

    @Override
    public int getPort() {
        return port;
    }
}
