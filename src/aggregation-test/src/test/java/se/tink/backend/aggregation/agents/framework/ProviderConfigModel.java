package se.tink.backend.aggregation.agents.framework;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import se.tink.backend.aggregation.rpc.Provider;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderConfigModel {
    private String market;
    private String currency;
    private List<Provider> providers;

    public String getMarket() {
        return market;
    }

    public String getCurrency() {
        return currency;
    }

    public List<Provider> getProviders() {
        return providers;
    }

    public Provider getProvider(String providerName) {
        return providers.stream()
                .filter(p -> providerName.equals(p.getName()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
