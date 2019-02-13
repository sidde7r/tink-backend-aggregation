package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.configuration.integrations.FinTsIntegrationConfiguration;
import se.tink.backend.aggregation.configuration.integrations.ICSConfiguration;
import se.tink.backend.aggregation.configuration.integrations.MonzoConfiguration;
import se.tink.backend.aggregation.configuration.integrations.NordeaConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabClientConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabConfiguration;
import se.tink.backend.aggregation.configuration.integrations.StarlingConfiguration;

import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrationsConfiguration {

    @JsonProperty private SbabConfiguration sbab;

    @JsonProperty private Map<String, MonzoConfiguration> monzo;

    @JsonProperty private Map<String, StarlingConfiguration> starling;

    @JsonProperty private FinTsIntegrationConfiguration fints;

    @JsonProperty private String ukOpenBankingJson;

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

    public Optional<MonzoConfiguration> getMonzo(String clientName) {
        return getClientConfiguration(clientName, monzo);
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

    public Optional<NordeaConfiguration> getNordea(String clientName) {
        return getClientConfiguration(clientName, nordea);
    }

    public String getProxyUri() {
        return proxyUri;
    }

    public Optional<ICSConfiguration> getIcsConfiguration(String clientName) {
        return getClientConfiguration(clientName, icsConfiguration);
    }
}
