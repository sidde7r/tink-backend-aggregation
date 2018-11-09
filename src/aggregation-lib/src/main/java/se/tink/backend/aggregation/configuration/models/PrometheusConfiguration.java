package se.tink.backend.aggregation.configuration.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrometheusConfiguration implements se.tink.libraries.metrics.PrometheusConfiguration {
    @JsonProperty
    private int port = 0;

    public int getPort() {
        return port;
    }
}
