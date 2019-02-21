package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.integrations.FinTsIntegrationConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabClientConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabConfiguration;

@JsonObject
public class IntegrationsConfiguration {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Map<String, Map<String, Object>> integrations = new HashMap<>();
    @JsonProperty private SbabConfiguration sbab;
    @JsonProperty private FinTsIntegrationConfiguration fints;
    @JsonProperty private String ukOpenBankingJson;
    @JsonProperty private String proxyUri;

    public Optional<SbabClientConfiguration> getSbab(String clientName) {
        return Optional.ofNullable(sbab)
                .flatMap(sc -> getClientConfiguration(clientName, sc.getClients()));
    }

    private <T> Optional<T> getClientConfiguration(String clientName, Map<String, T> configMap) {
        return Optional.ofNullable(configMap).map(m -> m.getOrDefault(clientName, null));
    }

    public FinTsIntegrationConfiguration getFinTsIntegrationConfiguration() {
        return fints;
    }

    public String getUkOpenBankingJson() {
        return ukOpenBankingJson;
    }

    public Optional<Map<String, Object>> getIntegration(String integrationName) {
        return Optional.ofNullable(integrations.get(integrationName));
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
