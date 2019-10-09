package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BALANCE_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BULK_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_TRANSACTIONS_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.authenticator.rpc.AccountPermissionResponseV31;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.identity.IdentityDataV31Response;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingV31AisConfiguration implements UkOpenBankingAisConfig {
    protected final URL apiBaseURL;
    private final URL wellKnownURL;
    private final URL identityDataURL;
    private final URL appToAppURL;
    private final List<String> additionalPermissions;
    private IdentityDataV31Response identityData;
    private String holderName;

    private UkOpenBankingV31AisConfiguration(
            URL apiBaseURL,
            URL wellKnownURL,
            URL identityDataURL,
            URL appToAppURL,
            List<String> additionalPermissions) {
        this.apiBaseURL = apiBaseURL;
        this.wellKnownURL = wellKnownURL;
        this.identityDataURL = identityDataURL;
        this.appToAppURL = appToAppURL;
        this.additionalPermissions = additionalPermissions;
    }

    public URL getWellKnownURL() {
        return wellKnownURL;
    }

    public URL getIdentityDataURL() {
        return identityDataURL;
    }

    public URL getAppToAppURL() {
        return appToAppURL;
    }

    public List<String> getAdditionalPermissions() {
        return additionalPermissions;
    }

    @Override
    public IdentityDataV31Response getIdentityData() {
        return identityData;
    }

    @Override
    public void setIdentityData(IdentityDataV31Response identityData) {
        this.identityData = identityData;
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
        return apiBaseURL.concat(ACCOUNT_BULK_REQUEST);
    }

    @Override
    public URL getAccountBalanceRequestURL(String accountId) {
        return apiBaseURL.concat(String.format(ACCOUNT_BALANCE_REQUEST, accountId));
    }

    @Override
    public URL createConsentRequestURL() {
        return apiBaseURL.concat(UkOpenBankingV31Constants.ApiServices.CONSENT_REQUEST);
    }

    @Override
    public URL getUpcomingTransactionRequestURL(String accountId) {
        return apiBaseURL.concat(String.format(ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST, accountId));
    }

    @Override
    public String getInitialTransactionsPaginationKey(String accountId) {
        return String.format(ACCOUNT_TRANSACTIONS_REQUEST, accountId);
    }

    public URL getApiBaseURL() {
        return apiBaseURL;
    }

    public static final class Builder {

        protected URL apiBaseURL;
        protected URL wellKnownURL;
        protected URL identityDataURL;
        protected URL appToAppURL;
        protected List<String> additionalPermissions;

        public Builder() {}

        public static Builder builder() {
            return new Builder();
        }

        public Builder withApiBaseURL(final String apiBaseURL) {
            this.apiBaseURL = new URL(apiBaseURL);
            return this;
        }

        public Builder withWellKnownURL(final String wellKnownURL) {
            this.wellKnownURL = new URL(wellKnownURL);
            return this;
        }

        public Builder withIdentityDataURL(final String identityDataURL) {
            this.identityDataURL = new URL(identityDataURL);
            return this;
        }

        public Builder withAppToAppURL(final String appToAppURL) {
            this.appToAppURL = new URL(appToAppURL);
            return this;
        }

        public Builder withAdditionalPermission(final String additionalPermission) {
            if (Objects.isNull(this.additionalPermissions)) {
                this.additionalPermissions = new ArrayList<>();
            }
            this.additionalPermissions.add(additionalPermission);
            return this;
        }

        public Builder withAdditionalPermissions(final List<String> additionalPermissions) {
            if (Objects.isNull(this.additionalPermissions)) {
                this.additionalPermissions = new ArrayList<>();
            }
            this.additionalPermissions.addAll(additionalPermissions);
            return this;
        }

        public UkOpenBankingV31AisConfiguration build() {
            Preconditions.checkNotNull(apiBaseURL);
            return new UkOpenBankingV31AisConfiguration(
                    apiBaseURL, wellKnownURL, identityDataURL, appToAppURL, additionalPermissions);
        }
    }
}
