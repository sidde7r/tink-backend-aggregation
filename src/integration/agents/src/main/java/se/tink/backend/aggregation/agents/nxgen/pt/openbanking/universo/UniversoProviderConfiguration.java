package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;

@Getter
public class UniversoProviderConfiguration extends Xs2aDevelopersProviderConfiguration {
    private String apiKey;
    private String keyId;
    private String qseal;

    public UniversoProviderConfiguration(
            String clientId,
            String baseUrl,
            String redirectUrl,
            String apiKey,
            String keyId,
            String qsealc) {
        super(clientId, baseUrl, redirectUrl);
        this.apiKey = apiKey;
        this.keyId = keyId;
        this.qseal = qsealc;
    }
}
