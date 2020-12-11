package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.rpc.AccountPermissionResponseV31;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.enums.MarketCode;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DanskebankAisConfiguration implements UkOpenBankingAisConfig {
    private final URL apiBaseURL;
    private final URL wellKnownURL;
    private final URL identityDataURL;
    private final Set<String> additionalPermissions;
    private final String market;
    private boolean partyEndpointEnabled;

    private DanskebankAisConfiguration(
            URL apiBaseURL,
            URL wellKnownURL,
            URL identityDataURL,
            Set<String> additionalPermissions,
            @Nonnull MarketCode market,
            boolean partyEndpointEnabled) {
        this.apiBaseURL = apiBaseURL;
        this.wellKnownURL = wellKnownURL;
        this.identityDataURL = identityDataURL;
        this.additionalPermissions = additionalPermissions;
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

    public Set<String> getAdditionalPermissions() {
        return additionalPermissions;
    }

    @Override
    public AccountOwnershipType getAllowedAccountOwnershipType() {
        return AccountOwnershipType.PERSONAL;
    }

    @Override
    public String getOrganisationId() {
        return "0015800000jf7AeAAI";
    }

    @Override
    public String getIntentId(AccountPermissionResponse accountPermissionResponse) {
        AccountPermissionResponseV31 accountPermissionResponseV31 =
                (AccountPermissionResponseV31) accountPermissionResponse;
        return accountPermissionResponseV31.getData().getAccountConsentId();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType() {
        return (Class<T>) AccountPermissionResponseV31.class;
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
        private Set<String> additionalPermissions;
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

        public DanskebankAisConfiguration.Builder withAdditionalPermission(
                final String additionalPermission) {
            if (Objects.isNull(this.additionalPermissions)) {
                this.additionalPermissions = new HashSet<>();
            }
            this.additionalPermissions.add(additionalPermission);
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
                    apiBaseURL,
                    wellKnownURL,
                    identityDataURL,
                    additionalPermissions,
                    market,
                    partyEndpointEnabled);
        }
    }
}
