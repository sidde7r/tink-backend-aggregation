package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Getter
public class UkOpenBankingPisConfiguration implements UkOpenBankingPisConfig {

    private final String baseUrl;

    private final URL wellKnownURL;

    public UkOpenBankingPisConfiguration(String pisBaseUrl, String wellKnownURL) {
        this.baseUrl = pisBaseUrl;
        this.wellKnownURL = new URL(wellKnownURL);
    }

    @Override
    public boolean useMaxAge() {
        return true;
    }
}
