package se.tink.backend.aggregation.cli.provider;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import se.tink.backend.core.ProviderConfiguration;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderConfigModel {
    private String market;
    private String currency;
    private List<ProviderConfiguration> providers;

    public String getMarket() {
        return market;
    }

    public String getCurrency() {
        return currency;
    }

    public List<ProviderConfiguration> getProviders() {
        return providers;
    }
}
