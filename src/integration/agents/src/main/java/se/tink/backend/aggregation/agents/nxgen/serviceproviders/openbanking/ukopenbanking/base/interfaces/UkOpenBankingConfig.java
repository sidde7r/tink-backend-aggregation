package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public interface UkOpenBankingConfig {
    public String getInitialTransactionsPaginationKey(String accountId);

    public URL getUpcomingTransactionRequestURL(URL apiBaseUrl, String accountId);

    public URL getBulkAccountRequestURL(URL apiBaseUrl);

    public URL getAccountBalanceRequestURL(URL apiBaseUrl, String accountId);

    <T extends AccountPermissionResponse> String getIntentId(T accountPermissionResponse);

    URL createConsentRequestURL(URL authBaseURL);

    URL createPaymentsURL(URL authBaseURL);

    URL createPaymentSubmissionURL(URL authBaseURL);

    <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType();
}
