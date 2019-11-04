package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BALANCE_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BULK_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_TRANSACTIONS_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST;

import com.google.common.base.Preconditions;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingV20AisConfiguration implements UkOpenBankingAisConfig {

    private final URL apiBaseURL;
    private final URL wellKnownURL;
    private final URL identityDataURL;
    private final URL appToAppURL;

    public UkOpenBankingV20AisConfiguration(
            URL apiBaseURL, URL wellKnownURL, URL identityDataURL, URL appToAppURL) {
        this.apiBaseURL = apiBaseURL;
        this.wellKnownURL = wellKnownURL;
        this.identityDataURL = identityDataURL;
        this.appToAppURL = appToAppURL;
    }

    public URL getBulkAccountRequestURL() {
        return apiBaseURL.concat(ACCOUNT_BULK_REQUEST);
    }

    public URL getAccountBalanceRequestURL(String accountId) {
        return apiBaseURL.concat(String.format(ACCOUNT_BALANCE_REQUEST, accountId));
    }

    @Override
    public String getIntentId(AccountPermissionResponse accountPermissionResponse) {
        return accountPermissionResponse.getData().getAccountRequestId();
    }

    @Override
    public URL createConsentRequestURL() {
        return apiBaseURL.concat(UkOpenBankingConstants.ApiServices.ACCOUNT_REQUESTS);
    }

    @Override
    public <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType() {
        // TODO: Check if this is possible to do without casting
        return (Class<T>) AccountPermissionResponse.class;
    }

    @Override
    public String getInitialTransactionsPaginationKey(String accountId) {
        return String.format(ACCOUNT_TRANSACTIONS_REQUEST, accountId);
    }

    @Override
    public URL getUpcomingTransactionRequestURL(String accountId) {
        return apiBaseURL.concat(String.format(ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST, accountId));
    }

    public URL getApiBaseURL() {
        return apiBaseURL;
    }

    @Override
    public URL getWellKnownURL() {
        return null;
    }

    @Override
    public URL getIdentityDataURL() {
        return null;
    }

    @Override
    public URL getAppToAppURL() {
        return null;
    }

    @Override
    public List<String> getAdditionalPermissions() {
        return null;
    }

    @Override
    public void setIdentityData(IdentityDataEntity identityData) {
        // soon V20 needs to be deprecated
    }

    @Override
    public IdentityDataEntity getIdentityData() {
        return null;
    }

    @Override
    public void setHolderName(String holderName) {
        // soon V20 needs to be deprecated
    }

    @Override
    public String getHolderName() {
        return null;
    }

    public static final class Builder {

        protected URL apiBaseURL;
        protected URL wellKnownURL;
        protected URL identityDataURL;
        protected URL appToAppURL;

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

        public UkOpenBankingV20AisConfiguration build() {
            Preconditions.checkNotNull(apiBaseURL);
            return new UkOpenBankingV20AisConfiguration(
                    apiBaseURL, wellKnownURL, identityDataURL, appToAppURL);
        }
    }
}
