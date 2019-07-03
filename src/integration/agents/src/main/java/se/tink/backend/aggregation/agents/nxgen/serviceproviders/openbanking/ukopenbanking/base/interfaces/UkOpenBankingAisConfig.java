package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public interface UkOpenBankingAisConfig {

    URL getBulkAccountRequestURL();

    URL getAccountBalanceRequestURL(String accountId);

    <T extends AccountPermissionResponse> String getIntentId(T accountPermissionResponse);

    URL createConsentRequestURL();

    <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType();

    String getInitialTransactionsPaginationKey(String accountId);

    URL getUpcomingTransactionRequestURL(String accountId);

    URL getAuthBaseURL();

    URL getApiBaseURL();
}
