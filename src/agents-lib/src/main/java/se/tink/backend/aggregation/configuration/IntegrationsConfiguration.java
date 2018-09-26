package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.configuration.integrations.FinTsIntegrationConfiguration;
import se.tink.backend.aggregation.configuration.integrations.MonzoConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabClientConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrationsConfiguration {
    @JsonProperty private SbabConfiguration sbab;
    @JsonProperty private Map<String, MonzoConfiguration> monzo;

    @JsonProperty private FinTsIntegrationConfiguration fints;
    @JsonProperty private String ukOpenBankingJson;

    @JsonProperty private String proxyUri;

    @JsonProperty private ICSConfiguration icsConfiguration;

    public SbabConfiguration getSbab() {
        return sbab;
    }

    private <T> Optional<T> getClientConfiguration(String clientName, Map<String, T> configMap) {
        return Optional.ofNullable(configMap).map(m -> m.getOrDefault(clientName, null));
    }

    public Optional<MonzoConfiguration> getMonzo(String clientName) {
        return getClientConfiguration(clientName, monzo);
    }

    public Optional<SbabClientConfiguration> getSbab(String clientName) {
        return Optional.ofNullable(sbab).flatMap(sc -> getClientConfiguration(clientName, sc.getClients()));
    }

    public FinTsIntegrationConfiguration getFinTsIntegrationConfiguration() {
        return fints;
    }

    public String getUkOpenBankingJson() {
        return ukOpenBankingJson;
    }

    public String getProxyUri() {
        return proxyUri;
    }

    public ICSConfiguration getIcsConfiguration() {
        return icsConfiguration;
    }
}
