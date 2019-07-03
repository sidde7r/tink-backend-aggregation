package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BALANCE_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BULK_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_TRANSACTIONS_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UkOpenBankingV30AisConfiguration implements UkOpenBankingAisConfig {

    private final URL apiBaseURL;
    private final URL authBaseURL;

    public UkOpenBankingV30AisConfiguration(String apiBaseURL, String authBaseURL) {
        this.apiBaseURL = new URL(apiBaseURL);
        this.authBaseURL = new URL(authBaseURL);
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
        return authBaseURL.concat(UkOpenBankingConstants.ApiServices.ACCOUNT_REQUESTS);
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

    public URL getAuthBaseURL() {
        return authBaseURL;
    }
}
