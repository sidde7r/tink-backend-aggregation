package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.authenticator.rpc.AccountPermissionResponseV31;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.enums.MarketCode;

public class DanskebankAisConfiguration implements UkOpenBankingAisConfig {
    protected final URL apiBaseURL;
    private final URL wellKnownURL;
    private final URL identityDataURL;
    private final URL appToAppURL;
    private final List<String> additionalPermissions;
    private final String market;
    private IdentityDataEntity identityData;
    private String holderName;

    private DanskebankAisConfiguration(
            URL apiBaseURL,
            URL wellKnownURL,
            URL identityDataURL,
            URL appToAppURL,
            List<String> additionalPermissions,
            @Nonnull MarketCode market) {
        this.apiBaseURL = apiBaseURL;
        this.wellKnownURL = wellKnownURL;
        this.identityDataURL = identityDataURL;
        this.appToAppURL = appToAppURL;
        this.additionalPermissions = additionalPermissions;
        this.market = market.name().toLowerCase();
    }

    public URL getWellKnownURL() {
        return wellKnownURL;
    }

    public URL getIdentityDataURL() {
        return identityDataURL;
    }

    @Override
    public boolean isPartyEndpointEnabled() {
        return true;
    }

    @Override
    public boolean isAccountPartiesEndpointEnabled() {
        return false;
    }

    @Override
    public boolean isAccountPartyEndpointEnabled() {
        return false;
    }

    public URL getAppToAppURL() {
        return appToAppURL;
    }

    public List<String> getAdditionalPermissions() {
        return additionalPermissions;
    }

    @Override
    public void setIdentityData(IdentityDataEntity identityData) {
        this.identityData = identityData;
    }

    @Override
    public IdentityDataEntity getIdentityData() {
        return identityData;
    }

    @Override
    public String getHolderName() {
        return holderName;
    }

    @Override
    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    @Override
    public String getIntentId(AccountPermissionResponse accountPermissionResponse) {
        AccountPermissionResponseV31 accountPermissionResponseV31 =
                (AccountPermissionResponseV31) accountPermissionResponse;
        return accountPermissionResponseV31.getData().getAccountConsentId();
    }

    @Override
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
        return apiBaseURL.concat("/accounts").concat("/" + accountId).concat("/beneficiaries");
    }

    @Override
    public URL createConsentRequestURL() {
        return apiBaseURL
                .concat("/" + market)
                .concat(UkOpenBankingV31Constants.ApiServices.CONSENT_REQUEST);
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

        protected URL apiBaseURL;
        protected URL wellKnownURL;
        protected URL identityDataURL;
        protected URL appToAppURL;
        private MarketCode market;
        protected List<String> additionalPermissions;

        public Builder() {}

        public static DanskebankAisConfiguration.Builder builder() {
            return new DanskebankAisConfiguration.Builder();
        }

        public DanskebankAisConfiguration.Builder withApiBaseURL(final String apiBaseURL) {
            this.apiBaseURL = new URL(apiBaseURL);
            return this;
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

        public DanskebankAisConfiguration.Builder onMarket(@Nonnull MarketCode market) {
            this.market = market;
            return this;
        }

        public DanskebankAisConfiguration.Builder withAppToAppURL(final String appToAppURL) {
            this.appToAppURL = new URL(appToAppURL);
            return this;
        }

        public DanskebankAisConfiguration.Builder withAdditionalPermission(
                final String additionalPermission) {
            if (Objects.isNull(this.additionalPermissions)) {
                this.additionalPermissions = new ArrayList<>();
            }
            this.additionalPermissions.add(additionalPermission);
            return this;
        }

        public DanskebankAisConfiguration.Builder withAdditionalPermissions(
                final List<String> additionalPermissions) {
            if (Objects.isNull(this.additionalPermissions)) {
                this.additionalPermissions = new ArrayList<>();
            }
            this.additionalPermissions.addAll(additionalPermissions);
            return this;
        }

        public DanskebankAisConfiguration build() {
            Preconditions.checkNotNull(apiBaseURL);
            return new DanskebankAisConfiguration(
                    apiBaseURL,
                    wellKnownURL,
                    identityDataURL,
                    appToAppURL,
                    additionalPermissions,
                    market);
        }
    }
}
