package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration;

public class CmcicAgentConfig {

    private final String baseUrl;
    private final String basePath;
    private final String authBaseUrl;

    public CmcicAgentConfig(String baseUrl, String basePath, String authBaseUrl) {
        this.baseUrl = baseUrl;
        this.basePath = basePath;
        this.authBaseUrl = authBaseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getAuthBaseUrl() {
        return authBaseUrl;
    }
}
