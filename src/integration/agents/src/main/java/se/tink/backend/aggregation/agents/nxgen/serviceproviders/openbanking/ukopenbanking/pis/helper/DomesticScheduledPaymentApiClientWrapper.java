package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.domesticscheduled.DomesticScheduledPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentConsentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class DomesticScheduledPaymentApiClientWrapper implements ApiClientWrapper {

    private final UkOpenBankingPaymentApiClient apiClient;
    private final DomesticScheduledPaymentConverter domesticScheduledPaymentConverter;

    @Override
    public PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) {
        final DomesticScheduledPaymentConsentRequest request =
                createDomesticScheduledPaymentConsentRequest(paymentRequest);

        final DomesticScheduledPaymentConsentResponse response =
                apiClient.createDomesticScheduledPaymentConsent(request);

        return domesticScheduledPaymentConverter.convertConsentResponseDtoToTinkPaymentResponse(
                response);
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        final DomesticScheduledPaymentResponse response =
                apiClient.getDomesticScheduledPayment(paymentId);

        return domesticScheduledPaymentConverter.convertResponseDtoToPaymentResponse(response);
    }

    @Override
    public PaymentResponse getPaymentConsent(String consentId) {
        final DomesticScheduledPaymentConsentResponse response =
                apiClient.getDomesticScheduledPaymentConsent(consentId);

        return domesticScheduledPaymentConverter.convertConsentResponseDtoToTinkPaymentResponse(
                response);
    }

    @Override
    public PaymentResponse executePayment(
            PaymentRequest paymentRequest,
            String consentId,
            String endToEndIdentification,
            String instructionIdentification) {

        final DomesticScheduledPaymentRequest request =
                createDomesticScheduledPaymentRequest(
                        paymentRequest, consentId, instructionIdentification);

        final DomesticScheduledPaymentResponse response =
                apiClient.executeDomesticScheduledPayment(request);

        return domesticScheduledPaymentConverter.convertResponseDtoToPaymentResponse(response);
    }

    private DomesticScheduledPaymentConsentRequest createDomesticScheduledPaymentConsentRequest(
            PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();
        final DomesticScheduledPaymentInitiation initiation =
                createDomesticScheduledPaymentInitiation(payment, payment.getUniqueId());
        final DomesticScheduledPaymentConsentRequestData consentRequestData =
                new DomesticScheduledPaymentConsentRequestData(initiation);

        return new DomesticScheduledPaymentConsentRequest(consentRequestData);
    }

    private DomesticScheduledPaymentRequest createDomesticScheduledPaymentRequest(
            PaymentRequest paymentRequest, String consentId, String instructionIdentification) {
        final Payment payment = paymentRequest.getPayment();
        final DomesticScheduledPaymentInitiation initiation =
                createDomesticScheduledPaymentInitiation(payment, instructionIdentification);
        final DomesticScheduledPaymentRequestData requestData =
                createDomesticScheduledPaymentRequestData(consentId, initiation);

        return new DomesticScheduledPaymentRequest(requestData);
    }

    private DomesticScheduledPaymentRequestData createDomesticScheduledPaymentRequestData(
            String consentId, DomesticScheduledPaymentInitiation initiation) {
        return DomesticScheduledPaymentRequestData.builder()
                .consentId(consentId)
                .initiation(initiation)
                .build();
    }

    private DomesticScheduledPaymentInitiation createDomesticScheduledPaymentInitiation(
            Payment payment, String instructionIdentification) {
        return DomesticScheduledPaymentInitiation.builder()
                .debtorAccount(domesticScheduledPaymentConverter.getDebtorAccount(payment))
                .creditorAccount(domesticScheduledPaymentConverter.getCreditorAccount(payment))
                .instructedAmount(domesticScheduledPaymentConverter.getInstructedAmount(payment))
                .remittanceInformation(
                        domesticScheduledPaymentConverter.getRemittanceInformation(payment))
                .requestedExecutionDateTime(
                        domesticScheduledPaymentConverter.getRequestedExecutionDateTime(payment))
                .instructionIdentification(instructionIdentification)
                .build();
    }
}
