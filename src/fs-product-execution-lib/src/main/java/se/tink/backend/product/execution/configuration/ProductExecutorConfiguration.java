package se.tink.backend.product.execution.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import java.util.Map;
import se.tink.backend.common.application.mortgage.MortgageProvider;
import se.tink.backend.common.config.CoordinationConfiguration;
import se.tink.backend.common.config.IntegrationsConfiguration;
import se.tink.libraries.endpoints.EndpointsConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductExecutorConfiguration extends Configuration {
    @JsonProperty
    private IntegrationsConfiguration integrations = new IntegrationsConfiguration();
    @JsonProperty
    private EndpointsConfiguration endpoints = new EndpointsConfiguration();
    @JsonProperty
    private CoordinationConfiguration coordination = new CoordinationConfiguration();

    public IntegrationsConfiguration getIntegrations() {
        return integrations;
    }

    public EndpointsConfiguration getEndpoints() {
        return endpoints;
    }

    public CoordinationConfiguration getCoordination() {
        return coordination;
    }

    public String getMortgageURI(MortgageProvider provider) {
        switch (provider) {
        case SEB_BANKID:
            return String.format(
                    "%s://%s/tink/loans/%s/mortgages",
                    integrations.getSeb().getMortgage().isHttps() ? "https" : "http",
                    integrations.getSeb().getMortgage().getTargetHost(),
                    integrations.getSeb().getMortgage().getApiVersion());
        case SBAB_BANKID:
            return String.format(
                    "%s://%s",
                    integrations.getSeb().getMortgage().isHttps() ? "https" : "http",
                    integrations.getSeb().getMortgage().getTargetHost());
        case UNKNOWN:
        default:
            throw new IllegalArgumentException("Not supported provider " + provider.name());
        }
    }

    public Map<String, String> getSEBMortgageHttpHeaders() {
        return integrations.getSeb().getMortgage().getHttpHeaders();
    }
}
