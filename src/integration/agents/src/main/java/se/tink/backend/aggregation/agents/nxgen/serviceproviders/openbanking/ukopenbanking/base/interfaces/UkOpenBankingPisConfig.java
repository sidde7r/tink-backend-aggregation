package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface UkOpenBankingPisConfig {

    URL createDomesticPaymentConsentURL();

    URL getDomesticPaymentConsentURL(String consentId);

    URL getDomesticFundsConfirmationURL(String consentId);

    URL createDomesticPaymentURL();

    URL getDomesticPayment(String domesticPaymentId);

    URL createDomesticScheduledPaymentConsentURL();

    URL getDomesticScheduledPaymentConsentURL(String consentId);

    URL createDomesticScheduledPaymentURL();

    URL getDomesticScheduledPayment(String paymentId);

    URL createInternationalPaymentConsentURL();

    URL getInternationalFundsConfirmationURL(String consentId);

    URL getInternationalPaymentConsentURL(String consentId);

    URL createInternationalPaymentURL();

    URL getInternationalPayment(String internationalPaymentId);
}
