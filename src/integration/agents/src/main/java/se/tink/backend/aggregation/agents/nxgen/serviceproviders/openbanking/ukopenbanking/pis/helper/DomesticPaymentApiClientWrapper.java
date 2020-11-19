package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.domestic.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.domestic.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.international.FundsConfirmationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@RequiredArgsConstructor
public class DomesticPaymentApiClientWrapper implements ApiClientWrapper {

    private final UkOpenBankingPaymentApiClient apiClient;

    private static void validateDomesticPaymentConsentResponse(
            DomesticPaymentConsentResponse response) {
        // Our flow has hardcoded a SCA redirect after this request so we can only continue if
        // the status is AwaitingAuthorisation.
        if (!response.hasStatusAwaitingAuthorisation()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Consent resource status was %s, expected status AwaitingAuthorisation.",
                            response.getData().getStatus()));
        }
    }

    @Override
    public PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) {
        final DomesticPaymentConsentRequest consentRequest =
                new DomesticPaymentConsentRequest(paymentRequest.getPayment());

        final DomesticPaymentConsentResponse response =
                apiClient.createDomesticPaymentConsent(consentRequest);

        validateDomesticPaymentConsentResponse(response);

        return response.toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        return apiClient.getDomesticPayment(paymentId).toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse getPaymentConsent(String consentId) {
        return apiClient.getDomesticPaymentConsent(consentId).toTinkPaymentResponse();
    }

    @Override
    public Optional<FundsConfirmationResponse> getFundsConfirmation(String consentId) {
        return Optional.of(apiClient.getDomesticFundsConfirmation(consentId));
    }

    @Override
    public PaymentResponse executePayment(
            PaymentRequest paymentRequest,
            String consentId,
            String endToEndIdentification,
            String instructionIdentification) {

        final DomesticPaymentRequest request =
                new DomesticPaymentRequest(
                        paymentRequest.getPayment(),
                        consentId,
                        endToEndIdentification,
                        instructionIdentification);

        return apiClient.executeDomesticPayment(request).toTinkPaymentResponse();
    }
}
