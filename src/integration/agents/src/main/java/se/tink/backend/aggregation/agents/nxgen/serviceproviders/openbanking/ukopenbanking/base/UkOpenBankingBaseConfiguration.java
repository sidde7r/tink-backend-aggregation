package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.nxgen.http.URL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BALANCE_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BULK_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_TRANSACTIONS_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST;

public class UkOpenBankingBaseConfiguration implements UkOpenBankingConfig {
    public URL getBulkAccountRequestURL(URL apiBaseUrl) {
        return apiBaseUrl.concat(ACCOUNT_BULK_REQUEST);
    }

    public URL getAccountBalanceRequestURL(URL apiBaseUrl, String accountId) {
        return apiBaseUrl.concat(String.format(ACCOUNT_BALANCE_REQUEST, accountId));
    }

    @Override
    public String getIntentId(AccountPermissionResponse accountPermissionResponse) {
        return accountPermissionResponse.getData().getAccountRequestId();
    }

    @Override
    public URL createConsentRequestURL(URL authBaseURL) {
        return authBaseURL.concat(UkOpenBankingConstants.ApiServices.ACCOUNT_REQUESTS);
    }

    @Override
    public URL createPaymentsURL(URL pisConsentURL) {
        return pisConsentURL.concat(UkOpenBankingConstants.ApiServices.PAYMENTS);
    }

    @Override
    public URL createPaymentSubmissionURL(URL pisBaseUrl) {
        return pisBaseUrl.concat(UkOpenBankingConstants.ApiServices.PAYMENT_SUBMISSIONS);
    }

    @Override
    public <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType() {
        //TODO: Check if this is possible to do without casting
        return (Class<T>) AccountPermissionResponse.class;
    }

    public String getInitialTransactionsPaginationKey(String accountId) {
        return String.format(ACCOUNT_TRANSACTIONS_REQUEST, accountId);
    }

    public URL getUpcomingTransactionRequestURL(URL apiBaseUrl, String accountId) {
        return apiBaseUrl.concat(String.format(ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST, accountId));
    }
}
