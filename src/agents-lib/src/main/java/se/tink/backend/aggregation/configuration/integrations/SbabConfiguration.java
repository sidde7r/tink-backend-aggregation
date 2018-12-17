package se.tink.backend.aggregation.configuration.integrations;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class SbabConfiguration {
    @JsonProperty private String signBaseUrl;
    @JsonProperty private Map<String, SbabClientConfiguration> clients;

    public String getSignBaseUrl() {
        return signBaseUrl;
    }

    public Map<String, SbabClientConfiguration> getClients() {
        return Optional.ofNullable(clients).orElse(Collections.emptyMap());
    }
}
