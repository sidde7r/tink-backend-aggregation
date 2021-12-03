package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.Risk;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.enums.MarketCode;

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

    @Override
    public boolean compatibleWithFundsConfirming() {
        return true;
    }

    @Override
    public Risk getPaymentContext() {
        return new Risk();
    }

    @Override
    public MarketCode getMarketCode() {
        return null;
    }
}
