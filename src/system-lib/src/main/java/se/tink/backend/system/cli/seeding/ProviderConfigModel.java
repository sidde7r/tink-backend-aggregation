package se.tink.backend.system.cli.seeding;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import se.tink.backend.core.Provider;

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
}
