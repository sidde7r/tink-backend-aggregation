package se.tink.backend.webhook.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import io.dropwizard.Configuration;
import java.util.List;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.PrometheusConfiguration;

public class WebhookConfiguration extends Configuration {

    private static final Joiner COMMA_JOINER = Joiner.on(",").skipNulls();

    @JsonProperty
    private List<String> queueHosts;

    @JsonProperty
    private PrometheusConfiguration prometheus = new PrometheusConfiguration();

    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    String getQueueHosts() {
        Preconditions.checkNotNull(queueHosts, "Missing queueHosts configuration");

        return COMMA_JOINER.join(queueHosts);
    }

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public PrometheusConfiguration getPrometheus() {
        return prometheus;
    }
}
