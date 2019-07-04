package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BALANCE_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BULK_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_TRANSACTIONS_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.authenticator.rpc.AccountPermissionResponseV31;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UkOpenBankingV31AisConfiguration implements UkOpenBankingAisConfig {
    protected final URL apiBaseURL;
    protected final URL authBaseURL;

    public UkOpenBankingV31AisConfiguration(String apiBaseURL, String authBaseURL) {
        this.apiBaseURL = new URL(apiBaseURL);
        this.authBaseURL = new URL(authBaseURL);
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
        return authBaseURL.concat(UkOpenBankingV31Constants.ApiServices.CONSENT_REQUEST);
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

    public URL getAuthBaseURL() {
        return authBaseURL;
    }
}
