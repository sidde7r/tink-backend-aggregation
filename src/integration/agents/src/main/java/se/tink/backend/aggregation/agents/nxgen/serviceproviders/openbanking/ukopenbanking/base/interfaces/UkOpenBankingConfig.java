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

    URL createDomesticPaymentConsentURL(URL pisBaseUrl);

    URL getDomesticPaymentConsentURL(URL pisBaseUrl, String consentId);

    URL getDomesticFundsConfirmationURL(URL pisBaseUrl, String consentId);

    URL createDomesticPaymentURL(URL pisBaseUrl);

    URL getDomesticPayment(URL pisBaseUrl, String domesticPaymentId);

    URL createInternationalPaymentConsentURL(URL pisBaseUrl);

    URL getInternationalFundsConfirmationURL(URL pisBaseUrl, String consentId);

    URL getInternationalPaymentConsentURL(URL pisBaseUrl, String consentId);

    URL createInternationalPaymentURL(URL pisBaseUrl);

    URL getInternationalPayment(URL pisBaseUrl, String internationalPaymentId);

    <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType();
}
