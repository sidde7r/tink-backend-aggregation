package se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationStorage;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderConfigWrapper {
    private String market;
    private String currency;
    private List<ProviderConfigurationStorage> providers;

    public String getMarket() {
        return market;
    }

    public String getCurrency() {
        return currency;
    }

    public List<ProviderConfigurationStorage> getProviders() {
        return providers;
    }
}
