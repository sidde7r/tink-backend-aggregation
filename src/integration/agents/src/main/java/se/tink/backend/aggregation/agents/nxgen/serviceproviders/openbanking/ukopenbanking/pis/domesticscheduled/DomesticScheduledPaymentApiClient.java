package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter.DomesticScheduledPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class DomesticScheduledPaymentApiClient implements UkOpenBankingPaymentApiClient {

    private final UkOpenBankingRequestBuilder requestBuilder;
    private final DomesticScheduledPaymentConverter domesticScheduledPaymentConverter;
    private final UkOpenBankingPisConfig pisConfig;

    @Override
    public PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) {
        final DomesticScheduledPaymentConsentRequest request =
                createDomesticScheduledPaymentConsentRequest(paymentRequest);

        final DomesticScheduledPaymentConsentResponse response =
                requestBuilder
                        .createPisRequestWithJwsHeader(
                                pisConfig.createDomesticScheduledPaymentConsentURL(), request)
                        .post(DomesticScheduledPaymentConsentResponse.class, request);

        return domesticScheduledPaymentConverter.convertConsentResponseDtoToTinkPaymentResponse(
                response);
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        final DomesticScheduledPaymentResponse response =
                requestBuilder
                        .createPisRequest(pisConfig.getDomesticScheduledPayment(paymentId))
                        .get(DomesticScheduledPaymentResponse.class);

        return domesticScheduledPaymentConverter.convertResponseDtoToPaymentResponse(response);
    }

    @Override
    public PaymentResponse getPaymentConsent(String consentId) {
        final DomesticScheduledPaymentConsentResponse response =
                requestBuilder
                        .createPisRequest(
                                pisConfig.getDomesticScheduledPaymentConsentURL(consentId))
                        .get(DomesticScheduledPaymentConsentResponse.class);

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
                requestBuilder
                        .createPisRequestWithJwsHeader(
                                pisConfig.createDomesticScheduledPaymentURL(), request)
                        .post(DomesticScheduledPaymentResponse.class, request);

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
