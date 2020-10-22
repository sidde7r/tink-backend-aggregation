package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.config;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international.FundsConfirmationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public class DomesticPisConfig implements UKPisConfig {

    private final UkOpenBankingApiClient client;
    private final UkOpenBankingPisConfig pisConfig;

    public DomesticPisConfig(UkOpenBankingApiClient client, UkOpenBankingPisConfig pisConfig) {
        this.client = client;
        this.pisConfig = pisConfig;
    }

    @Override
    public PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) {
        return client.createDomesticPaymentConsent(
                        pisConfig,
                        new DomesticPaymentConsentRequest(paymentRequest.getPayment()),
                        DomesticPaymentConsentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse fetchPayment(PaymentRequest paymentRequest) {
        // If payment has already been executed, fetch payment. Otherwise fetch consent
        String paymentId =
                paymentRequest.getStorage().get(UkOpenBankingV31Constants.Storage.PAYMENT_ID);

        if (!Strings.isNullOrEmpty(paymentId)) {
            return client.getDomesticPayment(pisConfig, paymentId, DomesticPaymentResponse.class)
                    .toTinkPaymentResponse();
        }

        String consentId = getConsentId(paymentRequest);

        return client.getDomesticPaymentConsent(
                        pisConfig, consentId, DomesticPaymentConsentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public FundsConfirmationResponse fetchFundsConfirmation(PaymentRequest paymentRequest) {
        String consentId = getConsentId(paymentRequest);

        return client.getDomesticFundsConfirmation(
                pisConfig, consentId, FundsConfirmationResponse.class);
    }

    @Override
    public PaymentResponse executePayment(
            PaymentRequest paymentRequest,
            String endToEndIdentification,
            String instructionIdentification) {
        String consentId = getConsentId(paymentRequest);

        DomesticPaymentRequest request =
                new DomesticPaymentRequest(
                        paymentRequest.getPayment(),
                        consentId,
                        endToEndIdentification,
                        instructionIdentification);
        if (client.isHSBCFamily()) {
            request.getData().getInitiation().setReferenceForHSBCFamily();
        }
        return client.executeDomesticPayment(pisConfig, request, DomesticPaymentResponse.class)
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
