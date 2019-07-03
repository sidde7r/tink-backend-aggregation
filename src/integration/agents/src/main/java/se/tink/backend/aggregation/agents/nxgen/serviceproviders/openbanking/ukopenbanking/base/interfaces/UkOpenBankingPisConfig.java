package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public interface UkOpenBankingPisConfig {

    URL createPaymentsURL();

    URL createPaymentSubmissionURL();

    URL createDomesticPaymentConsentURL();

    URL getDomesticPaymentConsentURL(String consentId);

    URL getDomesticFundsConfirmationURL(String consentId);

    URL createDomesticPaymentURL();

    URL getDomesticPayment(String domesticPaymentId);

    URL createInternationalPaymentConsentURL();

    URL getInternationalFundsConfirmationURL(String consentId);

    URL getInternationalPaymentConsentURL(String consentId);

    URL createInternationalPaymentURL();

    URL getInternationalPayment(String internationalPaymentId);

    <T extends AccountPermissionResponse> Class<T> getIntentIdResponseType();
}
