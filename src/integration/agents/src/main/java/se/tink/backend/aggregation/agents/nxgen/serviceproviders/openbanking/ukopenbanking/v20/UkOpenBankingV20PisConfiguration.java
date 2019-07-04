package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.authenticator.rpc.AccountPermissionResponseV31;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UkOpenBankingV20PisConfiguration implements UkOpenBankingPisConfig {

    private final URL pisBaseURL;
    private final URL pisConsentURL;

    public UkOpenBankingV20PisConfiguration(String pisBaseURL, String pisConsentURL) {
        this.pisBaseURL = new URL(pisBaseURL);
        this.pisConsentURL = new URL(pisConsentURL);
    }

    @Override
    public <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType() {
        return (Class<T>) AccountPermissionResponseV31.class;
    }

    @Override
    public URL createPaymentsURL() {
        return pisConsentURL.concat(UkOpenBankingConstants.ApiServices.PAYMENTS);
    }

    @Override
    public URL createPaymentSubmissionURL() {
        return pisBaseURL.concat(UkOpenBankingConstants.ApiServices.PAYMENT_SUBMISSIONS);
    }

    @Override
    public URL createDomesticPaymentConsentURL() {
        return pisBaseURL.concat(UkOpenBankingConstants.ApiServices.Domestic.PAYMENT_CONSENT);
    }

    @Override
    public URL getDomesticPaymentConsentURL(String consentId) {
        return pisBaseURL
                .concat(UkOpenBankingV31Constants.ApiServices.Domestic.PAYMENT_CONSENT_STATUS)
                .parameter(
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.consentId,
                        consentId);
    }

    @Override
    public URL getDomesticFundsConfirmationURL(String consentId) {
        return pisBaseURL
                .concat(UkOpenBankingConstants.ApiServices.Domestic.PAYMENT_FUNDS_CONFIRMATION)
                .parameter(
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.consentId,
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
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.paymentId,
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
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.consentId,
                        consentId);
    }

    @Override
    public URL getInternationalFundsConfirmationURL(String consentId) {
        return pisBaseURL
                .concat(UkOpenBankingConstants.ApiServices.International.PAYMENT_FUNDS_CONFIRMATION)
                .parameter(
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.consentId,
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
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.paymentId,
                        internationalPaymentId);
    }
}
