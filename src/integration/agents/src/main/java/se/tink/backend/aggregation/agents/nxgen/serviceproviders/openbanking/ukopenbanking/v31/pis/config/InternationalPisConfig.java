package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.config;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.FundsConfirmationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.InternationalPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.InternationalPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.InternationalPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.InternationalPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public class InternationalPisConfig implements UKPisConfig {

    private final UkOpenBankingApiClient client;
    private final UkOpenBankingPisConfig pisConfig;

    public InternationalPisConfig(UkOpenBankingApiClient client, UkOpenBankingPisConfig pisConfig) {
        this.client = client;
        this.pisConfig = pisConfig;
    }

    @Override
    public PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) {
        return client.createInternationalPaymentConsent(
                        pisConfig,
                        new InternationalPaymentConsentRequest(paymentRequest.getPayment()),
                        InternationalPaymentConsentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse fetchPayment(PaymentRequest paymentRequest) {
        // If paymentRequest has already been executed, fetch paymentRequest. Otherwise fetch
        // consent
        String paymentId =
                paymentRequest.getStorage().get(UkOpenBankingV31Constants.Storage.PAYMENT_ID);

        if (!Strings.isNullOrEmpty(paymentId)) {
            return client.getInternationalPayment(
                            pisConfig, paymentId, InternationalPaymentResponse.class)
                    .toTinkPaymentResponse();
        }

        String consentId = getConsentId(paymentRequest);

        return client.getInternationalPaymentConsent(
                        pisConfig, consentId, InternationalPaymentConsentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public FundsConfirmationResponse fetchFundsConfirmation(PaymentRequest paymentRequest) {
        String consentId = getConsentId(paymentRequest);

        return client.getInternationalFundsConfirmation(
                pisConfig, consentId, FundsConfirmationResponse.class);
    }

    @Override
    public PaymentResponse executePayment(
            PaymentRequest paymentRequest,
            String endToEndIdentification,
            String instructionIdentification) {
        String consentId = getConsentId(paymentRequest);

        InternationalPaymentRequest request =
                new InternationalPaymentRequest(
                        paymentRequest.getPayment(),
                        consentId,
                        endToEndIdentification,
                        instructionIdentification);

        return client.executeInternationalPayment(
                        pisConfig, request, InternationalPaymentResponse.class)
                .toTinkPaymentResponse();
    }

    private String getConsentId(PaymentRequest paymentRequest) {
        String consentId =
                paymentRequest.getStorage().get(UkOpenBankingV31Constants.Storage.CONSENT_ID);

        if (Strings.isNullOrEmpty(consentId)) {
            throw new IllegalStateException("consentId cannot be null or empty!");
        }

        return consentId;
    }
}
