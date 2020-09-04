package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration;

public class SamlinkAgentsConfiguration {

    private final String baseUrl;
    private final String baseOauthUrl;

    public SamlinkAgentsConfiguration(String baseUrl, String baseOauthUrl) {
        this.baseUrl = baseUrl;
        this.baseOauthUrl = baseOauthUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBaseOauthUrl() {
        return baseOauthUrl;
    }
}
