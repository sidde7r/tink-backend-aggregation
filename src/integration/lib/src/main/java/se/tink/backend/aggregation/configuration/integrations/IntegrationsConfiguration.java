package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IntegrationsConfiguration {
    private Map<String, Object> integrations = new HashMap<>();

    @JsonProperty private ImmutableList<String> proxyUris;

    public Optional<Object> getIntegration(String integrationName) {
        return Optional.ofNullable(integrations.get(integrationName));
    }

    public Optional<Object> getClientConfigurationAsObject(
            String integrationName, String clientName) {
        return getIntegration(integrationName)
                .filter(o -> o instanceof Map)
                .map(o -> (Map) o)
                .map(i -> i.get(clientName));
    }

    @JsonAnySetter
    private void addIntegration(String integrationName, Object integration) {
        integrations.put(integrationName, integration);
    }

    public ImmutableList<String> getProxyUris() {
        return proxyUris;
    }
}
