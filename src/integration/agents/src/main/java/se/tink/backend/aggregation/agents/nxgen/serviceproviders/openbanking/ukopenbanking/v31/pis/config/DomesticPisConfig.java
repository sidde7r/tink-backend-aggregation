package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.config;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.FundsConfirmationResponse;
import se.tink.libraries.payment.rpc.Payment;

public class DomesticPisConfig implements UKPisConfig {

    private final UkOpenBankingApiClient client;

    public DomesticPisConfig(UkOpenBankingApiClient client) {
        this.client = client;
    }

    @Override
    public Payment createPaymentConsent(Payment payment) throws PaymentException {
        return client.createDomesticPaymentConsent(
                        new DomesticPaymentConsentRequest(payment),
                        DomesticPaymentConsentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public Payment fetchPayment(Payment payment) throws PaymentException {
        // If payment has already been executed, fetch payment. Otherwise fetch consent
        String paymentId =
                payment
                        .getFromTemporaryStorage(UkOpenBankingV31Constants.Storage.PAYMENT_ID);

        if (!Strings.isNullOrEmpty(paymentId)) {
            return client.getDomesticPayment(paymentId, DomesticPaymentResponse.class)
                    .toTinkPayment();
        }

        String consentId = getConsentId(payment);

        return client.getDomesticPayment(consentId, DomesticPaymentConsentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public FundsConfirmationResponse fetchFundsConfirmation(Payment payment)
            throws PaymentException {
        String consentId = getConsentId(payment);

        return client.getDomesticFundsConfirmation(consentId, FundsConfirmationResponse.class);
    }

    @Override
    public Payment executePayment(
            Payment payment,
            String endToEndIdentification,
            String instructionIdentification)
            throws PaymentException {
        String consentId = getConsentId(payment);

        DomesticPaymentRequest request =
                new DomesticPaymentRequest(
                        payment,
                        consentId,
                        endToEndIdentification,
                        instructionIdentification);

        return client.executeDomesticPayment(request, DomesticPaymentResponse.class)
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
