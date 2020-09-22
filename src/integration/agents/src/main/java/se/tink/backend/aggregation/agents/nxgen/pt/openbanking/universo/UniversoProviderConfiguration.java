package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;

public class UniversoProviderConfiguration extends Xs2aDevelopersProviderConfiguration {
    private String apiKey;

    public UniversoProviderConfiguration(
            String clientId, String baseUrl, String redirectUrl, String apiKey) {
        super(clientId, baseUrl, redirectUrl);
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }
}
