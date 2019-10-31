package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IntegrationsConfiguration {
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Map<String, Object> integrations = new HashMap<>();

    @JsonProperty private String proxyUri;

    @JsonProperty private ImmutableList<String> proxyUris;

    public Optional<Object> getIntegration(String integrationName) {
        return Optional.ofNullable(integrations.get(integrationName));
    }

    public <T> Optional<T> getIntegration(String integrationName, Class<T> integrationConfigClass) {
        return getIntegration(integrationName)
                .map(i -> OBJECT_MAPPER.convertValue(i, integrationConfigClass));
    }

    public <T extends ClientConfiguration> Optional<T> getClientConfiguration(
            String integrationName, String clientName, Class<T> clientConfigClass) {
        return getIntegration(integrationName)
                .filter(o -> o instanceof Map)
                .map(o -> (Map) o)
                .map(i -> i.get(clientName))
                .map(c -> OBJECT_MAPPER.convertValue(c, clientConfigClass));
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

    public String getProxyUri() {
        return proxyUri;
    }

    public ImmutableList<String> getProxyUris() {
        return proxyUris;
    }
}
