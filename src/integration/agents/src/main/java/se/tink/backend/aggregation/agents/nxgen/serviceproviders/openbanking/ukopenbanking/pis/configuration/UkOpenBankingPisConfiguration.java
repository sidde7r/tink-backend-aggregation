package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingV31PaymentConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Getter
public class UkOpenBankingPisConfiguration implements UkOpenBankingPisConfig {

    private final String organisationId;

    private final URL pisBaseURL;

    private final URL wellKnownURL;

    public UkOpenBankingPisConfiguration(
            String organisationId, String pisBaseUrl, String wellKnownURL) {
        this.organisationId = organisationId;
        this.pisBaseURL = new URL(pisBaseUrl);
        this.wellKnownURL = new URL(wellKnownURL);
    }

    @Override
    public URL createDomesticPaymentConsentURL() {
        return pisBaseURL.concat(
                UkOpenBankingPaymentConstants.ApiServices.Domestic.PAYMENT_CONSENT);
    }

    @Override
    public URL getDomesticPaymentConsentURL(String consentId) {
        return pisBaseURL
                .concat(
                        UkOpenBankingV31PaymentConstants.ApiServices.Domestic
                                .PAYMENT_CONSENT_STATUS)
                .parameter(
                        UkOpenBankingV31PaymentConstants.ApiServices.UrlParameterKeys.CONSENT_ID,
                        consentId);
    }

    @Override
    public URL getDomesticFundsConfirmationURL(String consentId) {
        return pisBaseURL
                .concat(
                        UkOpenBankingPaymentConstants.ApiServices.Domestic
                                .PAYMENT_FUNDS_CONFIRMATION)
                .parameter(
                        UkOpenBankingV31PaymentConstants.ApiServices.UrlParameterKeys.CONSENT_ID,
                        consentId);
    }

    @Override
    public URL createDomesticPaymentURL() {
        return pisBaseURL.concat(UkOpenBankingPaymentConstants.ApiServices.Domestic.PAYMENT);
    }

    @Override
    public URL getDomesticPayment(String domesticPaymentId) {
        return pisBaseURL
                .concat(UkOpenBankingPaymentConstants.ApiServices.Domestic.PAYMENT_STATUS)
                .parameter(
                        UkOpenBankingV31PaymentConstants.ApiServices.UrlParameterKeys.PAYMENT_ID,
                        domesticPaymentId);
    }

    @Override
    public URL createDomesticScheduledPaymentConsentURL() {
        return pisBaseURL.concat(
                UkOpenBankingPaymentConstants.ApiServices.DomesticScheduled.PAYMENT_CONSENT);
    }

    @Override
    public URL getDomesticScheduledPaymentConsentURL(String consentId) {
        return pisBaseURL
                .concat(
                        UkOpenBankingV31PaymentConstants.ApiServices.DomesticScheduled
                                .PAYMENT_CONSENT_STATUS)
                .parameter(
                        UkOpenBankingV31PaymentConstants.ApiServices.UrlParameterKeys.CONSENT_ID,
                        consentId);
    }

    @Override
    public URL createDomesticScheduledPaymentURL() {
        return pisBaseURL.concat(
                UkOpenBankingPaymentConstants.ApiServices.DomesticScheduled.PAYMENT);
    }

    @Override
    public URL getDomesticScheduledPayment(String paymentId) {
        return pisBaseURL
                .concat(UkOpenBankingPaymentConstants.ApiServices.DomesticScheduled.PAYMENT_STATUS)
                .parameter(
                        UkOpenBankingV31PaymentConstants.ApiServices.UrlParameterKeys.PAYMENT_ID,
                        paymentId);
    }
}
