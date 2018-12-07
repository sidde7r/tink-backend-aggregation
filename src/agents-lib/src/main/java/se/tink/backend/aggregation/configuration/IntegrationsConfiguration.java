package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Environment;
import se.tink.backend.aggregation.configuration.integrations.FinTsIntegrationConfiguration;
import se.tink.backend.aggregation.configuration.integrations.MonzoConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabLegacyConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrationsConfiguration {
    @JsonProperty private SbabLegacyConfiguration sbabLegacy;
    @JsonProperty private Map<String, SbabConfiguration> sbab;

    @JsonProperty private String ukOpenBankingJson;

    @JsonProperty private FinTsIntegrationConfiguration fints;

    @JsonProperty private Map<String, MonzoConfiguration> monzo;

    public SbabLegacyConfiguration getSbabLegacy() {
        return sbabLegacy;
    }

    @JsonProperty private String proxyUri;

    public Optional<SbabConfiguration> getSbab(Environment environment) {
        return Optional.ofNullable(sbab).map(m -> m.getOrDefault(environment.toString(), null));
    }

    public String getUkOpenBankingJson() {
        return ukOpenBankingJson;
    }

    public Optional<MonzoConfiguration> getMonzo(String clientName) {
        if (Objects.isNull(monzo)) {
            return Optional.empty();
        }
        return Optional.ofNullable(monzo.getOrDefault(clientName, null));
    }

    public String getProxyUri() {
        return proxyUri;
    }

    public FinTsIntegrationConfiguration getFinTsIntegrationConfiguration() {
        return fints;
    }
}
