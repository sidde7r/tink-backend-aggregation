package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingPisConfiguration implements UkOpenBankingPisConfig {

    private final URL pisBaseURL;

    public UkOpenBankingPisConfiguration(String pisBaseUrl) {
        this.pisBaseURL = new URL(pisBaseUrl);
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
    public URL createDomesticScheduledPaymentConsentURL() {
        return pisBaseURL.concat(
                UkOpenBankingConstants.ApiServices.DomesticScheduled.PAYMENT_CONSENT);
    }

    @Override
    public URL getDomesticScheduledPaymentConsentURL(String consentId) {
        return pisBaseURL
                .concat(
                        UkOpenBankingV31Constants.ApiServices.DomesticScheduled
                                .PAYMENT_CONSENT_STATUS)
                .parameter(
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.CONSENT_ID,
                        consentId);
    }

    @Override
    public URL createDomesticScheduledPaymentURL() {
        return pisBaseURL.concat(UkOpenBankingConstants.ApiServices.DomesticScheduled.PAYMENT);
    }

    @Override
    public URL getDomesticScheduledPayment(String paymentId) {
        return pisBaseURL
                .concat(UkOpenBankingConstants.ApiServices.DomesticScheduled.PAYMENT_STATUS)
                .parameter(
                        UkOpenBankingV31Constants.ApiServices.UrlParameterKeys.PAYMENT_ID,
                        paymentId);
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
