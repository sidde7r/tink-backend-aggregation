package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.domestic.DomesticScheduledPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.domestic.DomesticScheduledPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.international.FundsConfirmationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@RequiredArgsConstructor
public class DomesticScheduledPaymentApiClientWrapper implements ApiClientWrapper {

    private final UkOpenBankingPaymentApiClient apiClient;

    @Override
    public PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) {
        final DomesticScheduledPaymentConsentRequest request =
                new DomesticScheduledPaymentConsentRequest(paymentRequest.getPayment());

        return apiClient.createDomesticScheduledPaymentConsent(request).toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        return apiClient.getDomesticScheduledPayment(paymentId).toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse getPaymentConsent(String consentId) {
        return apiClient.getDomesticScheduledPaymentConsent(consentId).toTinkPaymentResponse();
    }

    @Override
    public Optional<FundsConfirmationResponse> getFundsConfirmation(String consentId) {
        return Optional.empty();
    }

    @Override
    public PaymentResponse executePayment(
            PaymentRequest paymentRequest,
            String consentId,
            String endToEndIdentification,
            String instructionIdentification) {

        final DomesticScheduledPaymentRequest request =
                new DomesticScheduledPaymentRequest(
                        paymentRequest.getPayment(), consentId, instructionIdentification);

        return apiClient.executeDomesticScheduledPayment(request).toTinkPaymentResponse();
    }
}
