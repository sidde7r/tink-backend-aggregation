package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.domestic.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.FundsConfirmationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class DomesticPaymentApiClientWrapper implements ApiClientWrapper {

    private final UkOpenBankingPaymentApiClient apiClient;
    private final DomesticPaymentConverter domesticPaymentConverter;

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
                createDomesticScheduledPaymentConsentRequest(paymentRequest);

        final DomesticPaymentConsentResponse response =
                apiClient.createDomesticPaymentConsent(consentRequest);

        validateDomesticPaymentConsentResponse(response);

        return domesticPaymentConverter.convertConsentResponseDtoToTinkPaymentResponse(response);
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        final DomesticPaymentResponse response = apiClient.getDomesticPayment(paymentId);

        return domesticPaymentConverter.convertResponseDtoToPaymentResponse(response);
    }

    @Override
    public PaymentResponse getPaymentConsent(String consentId) {
        final DomesticPaymentConsentResponse response =
                apiClient.getDomesticPaymentConsent(consentId);
        return domesticPaymentConverter.convertConsentResponseDtoToTinkPaymentResponse(response);
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
                createDomesticPaymentRequest(
                        paymentRequest,
                        consentId,
                        endToEndIdentification,
                        instructionIdentification);

        final DomesticPaymentResponse response = apiClient.executeDomesticPayment(request);

        return domesticPaymentConverter.convertResponseDtoToPaymentResponse(response);
    }

    private DomesticPaymentConsentRequest createDomesticScheduledPaymentConsentRequest(
            PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();
        final DomesticPaymentInitiation initiation =
                createDomesticPaymentInitiation(
                        payment, payment.getUniqueIdForUKOPenBanking(), payment.getUniqueId());
        final DomesticPaymentConsentRequestData consentRequestData =
                new DomesticPaymentConsentRequestData(initiation);

        return new DomesticPaymentConsentRequest(consentRequestData);
    }

    private DomesticPaymentRequest createDomesticPaymentRequest(
            PaymentRequest paymentRequest,
            String consentId,
            String endToEndIdentification,
            String instructionIdentification) {
        final Payment payment = paymentRequest.getPayment();
        final DomesticPaymentInitiation initiation =
                createDomesticPaymentInitiation(
                        payment, endToEndIdentification, instructionIdentification);
        final DomesticPaymentRequestData requestData =
                createDomesticPaymentRequestData(consentId, initiation);

        return new DomesticPaymentRequest(requestData);
    }

    private DomesticPaymentRequestData createDomesticPaymentRequestData(
            String consentId, DomesticPaymentInitiation initiation) {
        return DomesticPaymentRequestData.builder()
                .consentId(consentId)
                .initiation(initiation)
                .build();
    }

    private DomesticPaymentInitiation createDomesticPaymentInitiation(
            Payment payment, String endToEndIdentification, String instructionIdentification) {
        return DomesticPaymentInitiation.builder()
                .debtorAccount(domesticPaymentConverter.getDebtorAccount(payment))
                .creditorAccount(domesticPaymentConverter.getCreditorAccount(payment))
                .instructedAmount(domesticPaymentConverter.getInstructedAmount(payment))
                .remittanceInformation(domesticPaymentConverter.getRemittanceInformation(payment))
                .instructionIdentification(instructionIdentification)
                .endToEndIdentification(endToEndIdentification)
                .build();
    }
}
