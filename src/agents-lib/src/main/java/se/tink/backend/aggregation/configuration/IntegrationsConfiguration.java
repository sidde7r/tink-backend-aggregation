package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.configuration.integrations.FinTsIntegrationConfiguration;
import se.tink.backend.aggregation.configuration.integrations.MonzoConfiguration;
import se.tink.backend.aggregation.configuration.integrations.SbabIntegrationConfiguration;

@JsonIgnoreProperties(ignoreUnknown =  true)
public class IntegrationsConfiguration {
    @JsonProperty
    private SbabIntegrationConfiguration sbab;

    @JsonProperty
    private String ukOpenBankingJson;

    @JsonProperty
    private Map<String, MonzoConfiguration> monzo;

    @JsonProperty
    private String proxyUri;

    @JsonProperty
    private FinTsIntegrationConfiguration fints;

    public SbabIntegrationConfiguration getSbab() {
        return sbab;
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
