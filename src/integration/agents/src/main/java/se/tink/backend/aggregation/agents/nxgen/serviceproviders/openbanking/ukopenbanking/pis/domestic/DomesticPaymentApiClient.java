package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.CONSENT_ID_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.ErrorMessage;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.PAYMENT_ID_KEY;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentFundsConfirmationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentFundsConfirmationResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.FundsAvailableResult;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@RequiredArgsConstructor
@Slf4j
public class DomesticPaymentApiClient implements UkOpenBankingPaymentApiClient {

    static final String PAYMENT_CONSENT = "/domestic-payment-consents";
    static final String PAYMENT_STATUS = "/domestic-payments/{paymentId}";
    static final String PAYMENT_CONSENT_STATUS = "/domestic-payment-consents/{consentId}";
    static final String PAYMENT_CONSENT_FUND_CONFIRMATION =
            "/domestic-payment-consents/{consentId}/funds-confirmation";
    static final String PAYMENT = "/domestic-payments";

    private final UkOpenBankingRequestBuilder requestBuilder;
    private final DomesticPaymentConverter domesticPaymentConverter;
    private final String baseUrl;

    @Override
    public PaymentResponse createPaymentConsent(PaymentRequest paymentRequest)
            throws PaymentException {
        final DomesticPaymentConsentRequest consentRequest =
                createDomesticPaymentConsentRequest(paymentRequest);
        DomesticPaymentConsentResponse response;
        try {
            response =
                    requestBuilder
                            .createPisRequestWithJwsHeader(createUrl(PAYMENT_CONSENT))
                            .post(DomesticPaymentConsentResponse.class, consentRequest);
            validateDomesticPaymentConsentResponse(response);

            return domesticPaymentConverter.convertConsentResponseDtoToTinkPaymentResponse(
                    response);
        } catch (HttpResponseException e) {
            HttpResponse httpResponse = e.getResponse();
            ErrorResponse body = httpResponse.getBody(ErrorResponse.class);
            if (body.getErrorMessages().contains(ErrorMessage.DEBTOR_VALIDATION_FAILURE)) {
                throw new DebtorValidationException(
                        DebtorValidationException.DEFAULT_MESSAGE,
                        InternalStatus.INVALID_SOURCE_ACCOUNT);
            }
            if (body.getErrorMessages().contains(ErrorMessage.CREDITOR_VALIDATION_FAILURE)) {
                throw new CreditorValidationException(
                        CreditorValidationException.DEFAULT_MESSAGE,
                        InternalStatus.INVALID_DESTINATION_ACCOUNT);
            }
            if (body.getErrorMessages().contains(ErrorMessage.INVALID_CLAIM_FAILURE)) {
                throw new PaymentRejectedException(
                        ErrorMessage.INVALID_CLAIM_FAILURE + ", Path = " + body.getErrorPaths(),
                        InternalStatus.INVALID_CLAIM_ERROR);
            }

            // To add more internal specific error exception
            throw e;
        }
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
            String instructionIdentification)
            throws PaymentException {
        final DomesticPaymentRequest request =
                createDomesticPaymentRequest(
                        paymentRequest,
                        consentId,
                        endToEndIdentification,
                        instructionIdentification);
        if (!areFundsAvailable(consentId)) {
            throw new InsufficientFundsException(
                    "Funds Availablity are not confirmed by the bank",
                    InternalStatus.INSUFFICIENT_FUNDS);
        }
        try {
            final DomesticPaymentResponse response =
                    requestBuilder
                            .createPisRequestWithJwsHeader(createUrl(PAYMENT))
                            .post(DomesticPaymentResponse.class, request);

            return domesticPaymentConverter.convertResponseDtoToPaymentResponse(response);
        } catch (HttpResponseException e) {
            HttpResponse httpResponse = e.getResponse();
            ErrorResponse body = httpResponse.getBody(ErrorResponse.class);
            if (body.getErrorMessages().contains(ErrorMessage.EXCEED_DAILY_LIMIT_FAILURE)) {
                throw new PaymentRejectedException(
                        ErrorMessage.EXCEED_DAILY_LIMIT_FAILURE,
                        InternalStatus.TRANSFER_LIMIT_REACHED);
            }
            // To add more internal specific error exception
            throw e;
        }
    }

    private boolean areFundsAvailable(String consentId) {
        DomesticPaymentConsentFundsConfirmationResponse response =
                requestBuilder
                        .createPisRequest(
                                createUrl(PAYMENT_CONSENT_FUND_CONFIRMATION)
                                        .parameter(CONSENT_ID_KEY, consentId))
                        .get(DomesticPaymentConsentFundsConfirmationResponse.class);
        boolean areFundsAvailable =
                Optional.ofNullable(response.getData())
                        .map(
                                DomesticPaymentConsentFundsConfirmationResponseData
                                        ::getFundsAvailableResult)
                        .map(FundsAvailableResult::areFundsAvailable)
                        .orElseGet(
                                () -> {
                                    log.warn("[UKOB] Problems During FundsConfirmation");
                                    return false;
                                });
        log.info("[UKOB] FundsConfirmation Result {}", areFundsAvailable);
        return areFundsAvailable;
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
