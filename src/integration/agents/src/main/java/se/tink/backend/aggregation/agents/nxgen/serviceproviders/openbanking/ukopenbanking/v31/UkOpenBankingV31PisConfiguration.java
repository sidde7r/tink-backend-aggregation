package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.authenticator.rpc.AccountPermissionResponseV31;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingV31PisConfiguration implements UkOpenBankingPisConfig {

    private final URL pisBaseURL;

    public UkOpenBankingV31PisConfiguration(String pisBaseUrl) {
        this.pisBaseURL = new URL(pisBaseUrl);
    }

    @Override
    public <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType() {
        return (Class<T>) AccountPermissionResponseV31.class;
    }

    // TODO: This is only called by PIS v1.1 so this should not be here (also pisConsentUrl
    // is used only by this function so if there is a way to derive this URL from pisBaseUrl
    // let's get rid of pisConsentUrl
    @Override
    public URL createPaymentsURL() {
        return pisBaseURL.concat(UkOpenBankingConstants.ApiServices.PAYMENTS);
    }

    @Override
    public URL createPaymentSubmissionURL() {
        return pisBaseURL.concat(UkOpenBankingConstants.ApiServices.PAYMENT_SUBMISSIONS);
    }

    // Payments V3

    @Override
    public URL createDomesticPaymentConsentURL() {
        return pisBaseURL.concat(UkOpenBankingConstants.ApiServices.Domestic.PAYMENT_CONSENT);
    }

    @Override
    public URL getDomesticPaymentConsentURL(String consentId) {
        return pisBaseURL
                .concat(UkOpenBankingV31Constants.ApiServices.Domestic.PAYMENT_CONSENT_STATUS)
                .parameter(
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.CONSENT_ID,
                        consentId);
    }

    @Override
    public URL getDomesticFundsConfirmationURL(String consentId) {
        return pisBaseURL
                .concat(UkOpenBankingConstants.ApiServices.Domestic.PAYMENT_FUNDS_CONFIRMATION)
                .parameter(
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.CONSENT_ID,
                        consentId);
    }

    @Override
    public URL createDomesticPaymentURL() {
        return pisBaseURL.concat(UkOpenBankingConstants.ApiServices.Domestic.PAYMENT);
    }

    @Override
    public URL getDomesticPayment(String domesticPaymentId) {
        return pisBaseURL
                .concat(UkOpenBankingConstants.ApiServices.Domestic.PAYMENT_STATUS)
                .parameter(
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.PAYMENT_ID,
                        domesticPaymentId);
    }

    @Override
    public URL createInternationalPaymentConsentURL() {
        return pisBaseURL.concat(UkOpenBankingConstants.ApiServices.International.PAYMENT_CONSENT);
    }

    @Override
    public URL getInternationalPaymentConsentURL(String consentId) {
        return pisBaseURL
                .concat(UkOpenBankingConstants.ApiServices.International.PAYMENT_CONSENT_STATUS)
                .parameter(
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.CONSENT_ID,
                        consentId);
    }

    @Override
    public URL getInternationalFundsConfirmationURL(String consentId) {
        return pisBaseURL
                .concat(UkOpenBankingConstants.ApiServices.International.PAYMENT_FUNDS_CONFIRMATION)
                .parameter(
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.CONSENT_ID,
                        consentId);
    }

    @Override
    public URL createInternationalPaymentURL() {
        return pisBaseURL.concat(UkOpenBankingConstants.ApiServices.International.PAYMENT);
    }

    @Override
    public URL getInternationalPayment(String internationalPaymentId) {
        return pisBaseURL
                .concat(UkOpenBankingConstants.ApiServices.International.PAYMENT_STATUS)
                .parameter(
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.PAYMENT_ID,
                        internationalPaymentId);
    }
}
