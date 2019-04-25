package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BALANCE_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_BULK_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_TRANSACTIONS_REQUEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.ApiServices.ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.nxgen.http.URL;

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
        // TODO: Check if this is possible to do without casting
        return (Class<T>) AccountPermissionResponse.class;
    }

    // Payments V3

    @Override
    public URL createDomesticPaymentConsentURL(URL pisBaseUrl) {
        return pisBaseUrl.concat(UkOpenBankingConstants.ApiServices.DOMESTIC_PAYMENT_CONSENTS);
    }

    @Override
    public URL getDomesticPaymentConsentURL(URL pisBaseUrl, String consentId) {
        return pisBaseUrl.concat(
                String.format(
                        UkOpenBankingConstants.ApiServices.DOMESTIC_PAYMENT_CONSENT_STATUS,
                        consentId));
    }

    @Override
    public URL getDomesticFundsConfirmationURL(URL pisBaseUrl, String consentId) {
        return pisBaseUrl.concat(
                String.format(
                        UkOpenBankingConstants.ApiServices.DOMESTIC_PAYMENT_FUNDS_CONFIRMATION,
                        consentId));
    }

    @Override
    public URL createDomesticPaymentURL(URL pisBaseUrl) {
        return pisBaseUrl.concat(UkOpenBankingConstants.ApiServices.DOMESTIC_PAYMENT);
    }

    @Override
    public URL getDomesticPayment(URL pisBaseUrl, String domesticPaymentId) {
        return pisBaseUrl.concat(
                String.format(
                        UkOpenBankingConstants.ApiServices.DOMESTIC_PAYMENT_STATUS,
                        domesticPaymentId));
    }

    @Override
    public URL createInternationalPaymentConsentURL(URL pisBaseUrl) {
        return pisBaseUrl.concat(UkOpenBankingConstants.ApiServices.INTERNATIONAL_PAYMENT_CONSENTS);
    }

    @Override
    public URL getInternationalPaymentConsentURL(URL pisBaseUrl, String consentId) {
        return pisBaseUrl.concat(
                String.format(
                        UkOpenBankingConstants.ApiServices.INTERNATIONAL_PAYMENT_CONSENT_STATUS,
                        consentId));
    }

    @Override
    public URL getInternationalFundsConfirmationURL(URL pisBaseUrl, String consentId) {
        return pisBaseUrl.concat(
                String.format(
                        UkOpenBankingConstants.ApiServices.INTERNATIONAL_PAYMENT_FUNDS_CONFIRMATION,
                        consentId));
    }

    @Override
    public URL createInternationalPaymentURL(URL pisBaseUrl) {
        return pisBaseUrl.concat(UkOpenBankingConstants.ApiServices.INTERNATIONAL_PAYMENT);
    }

    @Override
    public URL getInternationalPayment(URL pisBaseUrl, String internationalPaymentId) {
        return pisBaseUrl.concat(
                String.format(
                        UkOpenBankingConstants.ApiServices.INTERNATIONAL_PAYMENT_STATUS,
                        internationalPaymentId));
    }

    public String getInitialTransactionsPaginationKey(String accountId) {
        return String.format(ACCOUNT_TRANSACTIONS_REQUEST, accountId);
    }

    public URL getUpcomingTransactionRequestURL(URL apiBaseUrl, String accountId) {
        return apiBaseUrl.concat(String.format(ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST, accountId));
    }
}
