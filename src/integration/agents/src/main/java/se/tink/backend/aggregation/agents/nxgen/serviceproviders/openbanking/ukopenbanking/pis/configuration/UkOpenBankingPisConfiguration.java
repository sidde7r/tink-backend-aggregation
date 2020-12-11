package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Getter
public class UkOpenBankingPisConfiguration implements UkOpenBankingPisConfig {

    private final String organisationId;

    private final String baseUrl;

    private final URL wellKnownURL;

    public UkOpenBankingPisConfiguration(
            String organisationId, String pisBaseUrl, String wellKnownURL) {
        this.organisationId = organisationId;
        this.baseUrl = pisBaseUrl;
        this.wellKnownURL = new URL(wellKnownURL);
    }
}
