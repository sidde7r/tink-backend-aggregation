package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Environment;
import se.tink.backend.aggregation.configuration.integrations.FinTsIntegrationConfiguration;
import se.tink.backend.aggregation.configuration.integrations.MonzoConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabLegacyConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrationsConfiguration {
    @JsonProperty private SbabLegacyConfiguration sbabLegacy;
    @JsonProperty private Map<String, MonzoConfiguration> monzo;
    @JsonProperty private Map<String, SbabConfiguration> sbab;

    @JsonProperty private FinTsIntegrationConfiguration fints;
    @JsonProperty private String ukOpenBankingJson;

    @JsonProperty private String proxyUri;

    public SbabLegacyConfiguration getSbabLegacy() {
        return sbabLegacy;
    }

    private <T> Optional<T> getClientConfiguration(String key, Map<String, T> configMap) {
        return Optional.ofNullable(configMap).map(m -> m.getOrDefault(key, null));
    }

    public Optional<MonzoConfiguration> getMonzo(String clientName) {
        return getClientConfiguration(clientName, monzo);
    }

    public Optional<SbabConfiguration> getSbab(Environment environment) {
        return getClientConfiguration(environment.toString(), sbab);
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
}
