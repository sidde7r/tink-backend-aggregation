package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration;

public class DeutscheMarketConfiguration {
    private final String baseUrl;
    private final String psuIdType;

    public DeutscheMarketConfiguration(String baseUrl, String psuIdType) {
        this.baseUrl = baseUrl;
        this.psuIdType = psuIdType;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getPsuIdType() {
        return psuIdType;
    }
}
