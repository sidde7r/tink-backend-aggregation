package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.helper;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticScheduledPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticScheduledPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.international.FundsConfirmationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@RequiredArgsConstructor
public class DomesticScheduledPaymentApiClientWrapper implements ApiClientWrapper {

    private final UkOpenBankingApiClient apiClient;

    @Override
    public PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) {
        final DomesticScheduledPaymentConsentRequest request =
                new DomesticScheduledPaymentConsentRequest(paymentRequest.getPayment());

        return apiClient
                .createDomesticScheduledPaymentConsent(
                        request, DomesticScheduledPaymentConsentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        return apiClient
                .getDomesticScheduledPayment(paymentId, DomesticScheduledPaymentResponse.class)
                .toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse getPaymentConsent(String consentId) {
        return apiClient
                .getDomesticScheduledPaymentConsent(
                        consentId, DomesticScheduledPaymentConsentResponse.class)
                .toTinkPaymentResponse();
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

        return apiClient
                .executeDomesticScheduledPayment(request, DomesticScheduledPaymentResponse.class)
                .toTinkPaymentResponse();
    }
}
