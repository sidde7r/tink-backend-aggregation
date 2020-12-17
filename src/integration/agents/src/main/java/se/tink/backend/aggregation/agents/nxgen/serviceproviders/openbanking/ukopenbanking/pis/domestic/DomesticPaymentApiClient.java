package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.CONSENT_ID_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.PAYMENT_ID_KEY;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class DomesticPaymentApiClient implements UkOpenBankingPaymentApiClient {

    static final String PAYMENT_CONSENT = "/domestic-payment-consents";
    static final String PAYMENT_STATUS = "/domestic-payments/{paymentId}";
    static final String PAYMENT_CONSENT_STATUS = "/domestic-payment-consents/{consentId}";
    static final String PAYMENT = "/domestic-payments";

    private final UkOpenBankingRequestBuilder requestBuilder;
    private final DomesticPaymentConverter domesticPaymentConverter;
    private final String baseUrl;

    @Override
    public PaymentResponse createPaymentConsent(PaymentRequest paymentRequest) {
        final DomesticPaymentConsentRequest consentRequest =
                createDomesticPaymentConsentRequest(paymentRequest);

        final DomesticPaymentConsentResponse response =
                requestBuilder
                        .createPisRequestWithJwsHeader(createUrl(PAYMENT_CONSENT))
                        .post(DomesticPaymentConsentResponse.class, consentRequest);

        validateDomesticPaymentConsentResponse(response);

        return domesticPaymentConverter.convertConsentResponseDtoToTinkPaymentResponse(response);
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        final DomesticPaymentResponse response =
                requestBuilder
                        .createPisRequest(
                                createUrl(PAYMENT_STATUS).parameter(PAYMENT_ID_KEY, paymentId))
                        .get(DomesticPaymentResponse.class);

        return domesticPaymentConverter.convertResponseDtoToPaymentResponse(response);
    }

    @Override
    public PaymentResponse getPaymentConsent(String consentId) {
        final DomesticPaymentConsentResponse response =
                requestBuilder
                        .createPisRequest(
                                createUrl(PAYMENT_CONSENT_STATUS)
                                        .parameter(CONSENT_ID_KEY, consentId))
                        .get(DomesticPaymentConsentResponse.class);

        return domesticPaymentConverter.convertConsentResponseDtoToTinkPaymentResponse(response);
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

        final DomesticPaymentResponse response =
                requestBuilder
                        .createPisRequestWithJwsHeader(createUrl(PAYMENT))
                        .post(DomesticPaymentResponse.class, request);

        return domesticPaymentConverter.convertResponseDtoToPaymentResponse(response);
    }

    private void validateDomesticPaymentConsentResponse(DomesticPaymentConsentResponse response) {
        // Our flow has hardcoded a SCA redirect after this request so we can only continue if
        // the status is AwaitingAuthorisation.
        if (!response.hasStatusAwaitingAuthorisation()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Consent resource status was %s, expected status AwaitingAuthorisation.",
                            response.getData().getStatus()));
        }
    }

    private DomesticPaymentConsentRequest createDomesticPaymentConsentRequest(
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
                .remittanceInformation(
                        domesticPaymentConverter.getRemittanceInformationDto(payment))
                .instructionIdentification(instructionIdentification)
                .endToEndIdentification(endToEndIdentification)
                .build();
    }

    private URL createUrl(String path) {
        return new URL(baseUrl + path);
    }
}
