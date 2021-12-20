package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.UkObScope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.ApiServices;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.enums.MarketCode;

public class DanskebankAisConfiguration implements UkOpenBankingAisConfig {
    private final URL apiBaseURL;
    private final URL wellKnownURL;
    private final URL identityDataURL;
    private final String market;
    private final boolean partyEndpointEnabled;

    private DanskebankAisConfiguration(
            URL apiBaseURL,
            URL wellKnownURL,
            URL identityDataURL,
            @Nonnull MarketCode market,
            boolean partyEndpointEnabled) {
        this.apiBaseURL = apiBaseURL;
        this.wellKnownURL = wellKnownURL;
        this.identityDataURL = identityDataURL;
        this.market = market.name().toLowerCase();
        this.partyEndpointEnabled = partyEndpointEnabled;
    }

    public URL getWellKnownURL() {
        return wellKnownURL;
    }

    public URL getIdentityDataURL() {
        return identityDataURL;
    }

    @Override
    public boolean isPartyEndpointEnabled() {
        return partyEndpointEnabled;
    }

    @Override
    public boolean isAccountPartiesEndpointEnabled() {
        return false;
    }

    @Override
    public boolean isAccountPartyEndpointEnabled() {
        return false;
    }

    @Override
    public ImmutableSet<UkObScope> getAvailablePermissions() {
        return ImmutableSet.<UkObScope>builder()
                .add(
                        UkObScope.READ_ACCOUNTS_DETAIL,
                        UkObScope.READ_BALANCES,
                        UkObScope.READ_BENEFICIARIES_DETAIL,
                        UkObScope.READ_TRANSACTIONS_CREDITS,
                        UkObScope.READ_TRANSACTIONS_DEBITS,
                        UkObScope.READ_TRANSACTIONS_DETAIL)
                .build();
    }

    @Override
    public String getOrganisationId() {
        return "0015800000jf7AeAAI";
    }

    @Override
    public URL getBulkAccountRequestURL() {
        return apiBaseURL.concat(ApiServices.ACCOUNT_BULK_REQUEST);
    }

    @Override
    public URL getAccountBalanceRequestURL(String accountId) {
        return apiBaseURL.concat(String.format(ApiServices.ACCOUNT_BALANCE_REQUEST, accountId));
    }

    @Override
    public URL getAccountBeneficiariesRequestURL(String accountId) {
        return apiBaseURL.concat(
                String.format(ApiServices.ACCOUNT_BENEFICIARIES_REQUEST, accountId));
    }

    @Override
    public URL createConsentRequestURL() {
        return apiBaseURL
                .concat("/" + market)
                .concat(UkOpenBankingV31Constants.ApiServices.CONSENT_REQUEST);
    }

    @Override
    public URL getConsentDetailsRequestURL(String consentId) {
        return apiBaseURL.concat(
                String.format(
                        UkOpenBankingV31Constants.ApiServices.CONSENT_DETAILS_REQUEST, consentId));
    }

    @Override
    public URL getUpcomingTransactionRequestURL(String accountId) {
        return apiBaseURL.concat(
                String.format(ApiServices.ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST, accountId));
    }

    @Override
    public String getInitialTransactionsPaginationKey(String accountId) {
        return String.format(ApiServices.ACCOUNT_TRANSACTIONS_REQUEST, accountId);
    }

    public URL getApiBaseURL() {
        return apiBaseURL;
    }

    public static final class Builder {

        private final URL apiBaseURL;
        private URL wellKnownURL;
        private URL identityDataURL;
        private final MarketCode market;
        private boolean partyEndpointEnabled = true;

        public Builder(final String apiBaseUrl, final MarketCode market) {
            this.apiBaseURL = new URL(apiBaseUrl);
            this.market = market;
        }

        public DanskebankAisConfiguration.Builder withWellKnownURL(final URL wellKnownURL) {
            this.wellKnownURL = wellKnownURL;
            return this;
        }

        public DanskebankAisConfiguration.Builder withIdentityDataURL(
                final String identityDataURL) {
            this.identityDataURL = new URL(identityDataURL);
            return this;
        }

        public DanskebankAisConfiguration.Builder partyEndpointEnabled(
                final boolean partyEndpointEnabled) {
            this.partyEndpointEnabled = partyEndpointEnabled;
            return this;
        }

        public DanskebankAisConfiguration build() {
            Preconditions.checkNotNull(apiBaseURL);
            return new DanskebankAisConfiguration(
                    apiBaseURL, wellKnownURL, identityDataURL, market, partyEndpointEnabled);
        }
    }
}
