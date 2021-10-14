package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration;

import lombok.Getter;

@Getter
public class DeutscheMarketConfiguration {
    private final String baseUrl;
    private final String psuIdType;

    public DeutscheMarketConfiguration(String baseUrl, String psuIdType) {
        this.baseUrl = baseUrl;
        this.psuIdType = psuIdType;
    }
}
