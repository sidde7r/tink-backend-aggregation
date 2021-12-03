package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.Risk;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.enums.MarketCode;

public class DanskeBankPisConfiguration implements UkOpenBankingPisConfig {

    private final URL apiBaseURL;
    private final URL wellKnownURL;
    private final MarketCode market;

    public DanskeBankPisConfiguration(URL apiBaseURL, URL wellKnownURL, MarketCode market) {
        this.apiBaseURL = apiBaseURL;
        this.wellKnownURL = wellKnownURL;
        this.market = market;
    }

    @Override
    public URL getWellKnownURL() {
        return wellKnownURL;
    }

    @Override
    public String getBaseUrl() {
        return apiBaseURL.toString();
    }

    @Override
    public boolean useMaxAge() {
        return false;
    }

    @Override
    public boolean compatibleWithFundsConfirming() {
        return false;
    }

    @Override
    public Risk getPaymentContext() {
        RiskDanskeBank riskDanskeBank = new RiskDanskeBank();
        riskDanskeBank.setPaymentContextCode("PartyToParty");
        return riskDanskeBank;
    }

    @Override
    public MarketCode getMarketCode() {
        return market;
    }

    public static final class Builder {

        private final URL apiBaseURL;
        private URL wellKnownURL;
        private final MarketCode market;

        public Builder(final String apiBaseUrl, final MarketCode market) {
            this.apiBaseURL = new URL(apiBaseUrl);
            this.market = market;
        }

        public DanskeBankPisConfiguration.Builder withWellKnownURL(final URL wellKnownURL) {
            this.wellKnownURL = wellKnownURL;
            return this;
        }

        public DanskeBankPisConfiguration build() {
            Preconditions.checkNotNull(apiBaseURL);
            return new DanskeBankPisConfiguration(apiBaseURL, wellKnownURL, market);
        }
    }
}
