package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.config;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.FundsConfirmationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.InternationalPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.InternationalPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.InternationalPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.InternationalPaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

public class InternationalPisConfig implements UKPisConfig {

    private final UkOpenBankingApiClient client;

    public InternationalPisConfig(UkOpenBankingApiClient client) {
        this.client = client;
    }

    @Override
    public Payment createPaymentConsent(Payment payment) throws PaymentException {
        return client.createInternationalPaymentConsent(
                        new InternationalPaymentConsentRequest(payment),
                        InternationalPaymentConsentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public Payment fetchPayment(Payment payment) throws PaymentException {
        // If payment has already been executed, fetch payment. Otherwise fetch consent
        String paymentId =
                payment.getFromTemporaryStorage(UkOpenBankingV31Constants.Storage.PAYMENT_ID);

        if (!Strings.isNullOrEmpty(paymentId)) {
            return client.getInternationalPayment(paymentId, InternationalPaymentResponse.class)
                    .toTinkPayment();
        }

        String consentId = getConsentId(payment);

        return client.getInternationalPaymentConsent(
                        consentId, InternationalPaymentConsentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public FundsConfirmationResponse fetchFundsConfirmation(Payment payment)
            throws PaymentException {
        String consentId = getConsentId(payment);

        return client.getInternationalFundsConfirmation(consentId, FundsConfirmationResponse.class);
    }

    @Override
    public Payment executePayment(
            Payment payment, String endToEndIdentification, String instructionIdentification)
            throws PaymentException {
        String consentId = getConsentId(payment);

        InternationalPaymentRequest request =
                new InternationalPaymentRequest(
                        payment, consentId, endToEndIdentification, instructionIdentification);

        return client.executeInternationalPayment(request, InternationalPaymentResponse.class)
                .toTinkPayment();
    }

    private String getConsentId(Payment payment) {
        String consentId =
                payment.getFromTemporaryStorage(UkOpenBankingV31Constants.Storage.CONSENT_ID);

        if (Strings.isNullOrEmpty(consentId)) {
            throw new IllegalStateException("consentId cannot be null or empty!");
        }

        return consentId;
    }
}
