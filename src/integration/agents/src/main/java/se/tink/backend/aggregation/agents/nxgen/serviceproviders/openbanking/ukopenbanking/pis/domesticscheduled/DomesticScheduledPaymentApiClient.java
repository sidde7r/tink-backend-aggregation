package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.CONSENT_ID_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.PAYMENT_ID_KEY;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingRequestBuilder;
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
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class DomesticScheduledPaymentApiClient implements UkOpenBankingPaymentApiClient {

    static final String PAYMENT_CONSENT = "/domestic-scheduled-payment-consents";
    static final String PAYMENT_CONSENT_STATUS = "/domestic-scheduled-payment-consents/{consentId}";
    static final String PAYMENT = "/domestic-scheduled-payments";
    static final String PAYMENT_STATUS = "/domestic-scheduled-payments/{paymentId}";

    private final UkOpenBankingRequestBuilder requestBuilder;
    private final DomesticScheduledPaymentConverter domesticScheduledPaymentConverter;
    private final String baseUrl;

    @Override
    public PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) {
        final DomesticScheduledPaymentConsentRequest request =
                createDomesticScheduledPaymentConsentRequest(paymentRequest);

        final DomesticScheduledPaymentConsentResponse response =
                requestBuilder
                        .createPisRequestWithJwsHeader(createUrl(PAYMENT_CONSENT))
                        .post(DomesticScheduledPaymentConsentResponse.class, request);

        return domesticScheduledPaymentConverter.convertConsentResponseDtoToTinkPaymentResponse(
                response);
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        final DomesticScheduledPaymentResponse response =
                requestBuilder
                        .createPisRequest(
                                createUrl(PAYMENT_STATUS).parameter(PAYMENT_ID_KEY, paymentId))
                        .get(DomesticScheduledPaymentResponse.class);

        return domesticScheduledPaymentConverter.convertResponseDtoToPaymentResponse(response);
    }

    @Override
    public PaymentResponse getPaymentConsent(String consentId) {
        final DomesticScheduledPaymentConsentResponse response =
                requestBuilder
                        .createPisRequest(
                                createUrl(PAYMENT_CONSENT_STATUS)
                                        .parameter(CONSENT_ID_KEY, consentId))
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
                        .createPisRequestWithJwsHeader(createUrl(PAYMENT))
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

    private URL createUrl(String path) {
        return new URL(baseUrl + path);
    }
}
