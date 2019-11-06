package se.tink.backend.aggregation.agents.banks.se.collector.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class CollectorConfiguration implements ClientConfiguration {
    @JsonProperty private String collectorSubscriptionKey;

    public String getCollectorSubscriptionKey() {
        Preconditions.checkNotNull(
                collectorSubscriptionKey,
                "Invalid configuration, collectorSubscriptionKey cannot be null.");
        return collectorSubscriptionKey;
    }
}
