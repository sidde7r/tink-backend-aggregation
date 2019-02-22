package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IntegrationsConfiguration {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Map<String, Map<String, Object>> integrations = new HashMap<>();
    @JsonProperty private String proxyUri;

    private Optional<Map<String, Object>> getIntegration(String integrationName) {
        return Optional.ofNullable(integrations.get(integrationName));
    }

    public <T> Optional<T> getIntegration(String integrationName, Class<T> integrationConfigClass) {
        return getIntegration(integrationName)
                .map(i -> OBJECT_MAPPER.convertValue(i, integrationConfigClass));
    }

    public <T extends ClientConfiguration> Optional<T> getClientConfiguration(
            String integrationName, String clientName, Class<T> clientConfigClass) {
        return getIntegration(integrationName)
                .map(i -> i.get(clientName))
                .map(c -> OBJECT_MAPPER.convertValue(c, clientConfigClass));
    }

    @JsonAnySetter
    public void addIntegration(String integrationName, Map<String, Object> clientConfigMap) {
        integrations.put(integrationName, clientConfigMap);
    }

    public String getProxyUri() {
        return proxyUri;
    }
}
