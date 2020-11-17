package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.international.FundsConfirmationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.international.InternationalPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.international.InternationalPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.international.InternationalPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.international.InternationalPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@RequiredArgsConstructor
public class InternationalPaymentApiClientWrapper implements ApiClientWrapper {

    private final UkOpenBankingPaymentApiClient apiClient;

    @Override
    public PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) {
        final InternationalPaymentConsentRequest consentRequest =
                new InternationalPaymentConsentRequest(paymentRequest.getPayment());

        return apiClient
                .createInternationalPaymentConsent(
                        consentRequest, InternationalPaymentConsentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        return apiClient
                .getInternationalPayment(paymentId, InternationalPaymentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse getPaymentConsent(String consentId) {
        return apiClient
                .getInternationalPaymentConsent(
                        consentId, InternationalPaymentConsentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public Optional<FundsConfirmationResponse> getFundsConfirmation(String consentId) {
        return Optional.of(
                apiClient.getInternationalFundsConfirmation(
                        consentId, FundsConfirmationResponse.class));
    }

    @Override
    public PaymentResponse executePayment(
            PaymentRequest paymentRequest,
            String consentId,
            String endToEndIdentification,
            String instructionIdentification) {

        final InternationalPaymentRequest request =
                new InternationalPaymentRequest(
                        paymentRequest.getPayment(),
                        consentId,
                        endToEndIdentification,
                        instructionIdentification);

        return apiClient
                .executeInternationalPayment(request, InternationalPaymentResponse.class)
                .toTinkPaymentResponse();
    }
}
