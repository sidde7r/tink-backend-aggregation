package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.configuration.integrations.FinTsIntegrationConfiguration;
import se.tink.backend.aggregation.configuration.integrations.ICSConfiguration;
import se.tink.backend.aggregation.configuration.integrations.NordeaConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabClientConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabConfiguration;
import se.tink.backend.aggregation.configuration.integrations.StarlingConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IntegrationsConfiguration {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty private SbabConfiguration sbab;

    @JsonProperty private Map<String, StarlingConfiguration> starling;

    @JsonProperty private FinTsIntegrationConfiguration fints;

    @JsonProperty private String ukOpenBankingJson;

    private Map<String, Map<String, Object>> integrations = new HashMap<>();
    @JsonProperty private String proxyUri;

    @JsonProperty private Map<String, ICSConfiguration> icsConfiguration;

    @JsonProperty private Map<String, NordeaConfiguration> nordea;

    public SbabConfiguration getSbab() {
        return sbab;
    }

    public Optional<SbabClientConfiguration> getSbab(String clientName) {
        return Optional.ofNullable(sbab)
                .flatMap(sc -> getClientConfiguration(clientName, sc.getClients()));
    }

    private <T> Optional<T> getClientConfiguration(String clientName, Map<String, T> configMap) {
        return Optional.ofNullable(configMap).map(m -> m.getOrDefault(clientName, null));
    }

    public Optional<StarlingConfiguration> getStarling(String clientName) {
        return getClientConfiguration(clientName, starling);
    }

    public FinTsIntegrationConfiguration getFinTsIntegrationConfiguration() {
        return fints;
    }

    public String getUkOpenBankingJson() {
        return ukOpenBankingJson;
    }
    
    public <T extends ClientConfiguration> Optional<T> getClientConfiguration(
            String integrationName, String clientName, Class<T> clientConfigClass) {
        return Optional.of(integrations.get(integrationName))
                .map(i -> i.get(clientName))
                .map(c -> OBJECT_MAPPER.convertValue(c, clientConfigClass));
    }

    public Optional<NordeaConfiguration> getNordea(String clientName) {
        return getClientConfiguration(clientName, nordea);
    }
    
    @JsonAnySetter
    public void addIntegration(String integrationName, Map<String, Object> clientConfigMap) {
        integrations.put(integrationName, clientConfigMap);
    }

    public String getProxyUri() {
        return proxyUri;
    }

    public Optional<ICSConfiguration> getIcsConfiguration(String clientName) {
        return getClientConfiguration(clientName, icsConfiguration);
    }
}
