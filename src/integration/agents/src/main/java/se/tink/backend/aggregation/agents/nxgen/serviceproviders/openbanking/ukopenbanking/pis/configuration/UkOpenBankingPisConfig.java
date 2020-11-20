package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration;

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

    URL getWellKnownURL();

    String getOrganisationId();
}
