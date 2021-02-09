package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.FormValues.DEFAULT_ZONE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.PaymentSteps.CONFIRM_PAYMENT_STEP;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.PaymentSteps.POST_SIGN_STEP;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PartyIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentInformationStatusCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.StatusReasonInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class SocieteGeneralePaymentExecutor implements PaymentExecutor {
    private final SocieteGeneraleApiClient apiClient;
    private final String redirectUrl;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private static final Logger logger =
            LoggerFactory.getLogger(SocieteGeneralePaymentExecutor.class);
    private final CountryDateHelper dateHelper;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        apiClient.fetchPisAccessToken();
        sessionStorage.put(
                SocieteGeneraleConstants.QueryKeys.STATE, strongAuthenticationState.getState());
        PaymentType type = PaymentType.SEPA;
        CreatePaymentRequest request = buildPaymentRequest(paymentRequest);
        CreatePaymentResponse paymentResponse = apiClient.createPayment(request);
        String authorizeUrl =
                Optional.ofNullable(paymentResponse.getLinks().getConsentApproval().getUrl())
                        .orElseThrow(
                                () -> {
                                    logger.error(
                                            "Payment authorization failed. There is no authentication url!");
                                    return new PaymentAuthorizationException(
                                            UserMessage.PAYMENT_AUTHORIZATION_FAILED.getKey().get(),
                                            new PaymentRejectedException());
                                });

        sessionStorage.put(SocieteGeneraleConstants.StorageKeys.AUTH_URL, authorizeUrl);

        return paymentResponse.toTinkPaymentResponse(type);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentMultiStepResponse paymentMultiStepResponse;
        Payment payment = paymentMultiStepRequest.getPayment();
        String paymentId = payment.getUniqueId();

        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                openThirdPartyApp(
                        new URL(sessionStorage.get(SocieteGeneraleConstants.StorageKeys.AUTH_URL)));
                waitForSupplementalInformation();
                paymentMultiStepResponse =
                        new PaymentMultiStepResponse(payment, POST_SIGN_STEP, new ArrayList<>());
                break;
            case POST_SIGN_STEP:
                GetPaymentResponse getPaymentResponse = fetchPaymentStatus(paymentId);
                paymentMultiStepResponse =
                        handleSignedPayment(getPaymentResponse, CONFIRM_PAYMENT_STEP);
                break;
            case CONFIRM_PAYMENT_STEP:
                GetPaymentResponse paymentConfirmationResponse =
                        apiClient.confirmPayment(paymentId);
                paymentMultiStepResponse =
                        handleSignedPayment(
                                paymentConfirmationResponse,
                                AuthenticationStepConstants.STEP_FINALIZE);
                break;
            default:
                logger.error(
                        "Payment failed due to unknown sign step{} ",
                        paymentMultiStepRequest.getStep());
                throw new PaymentException(UserMessage.PAYMENT_FAILED.getKey().get());
        }
        return paymentMultiStepResponse;
    }

    private PaymentMultiStepResponse handleSignedPayment(
            GetPaymentResponse paymentResponse, String nextStep) throws PaymentException {
        PaymentMultiStepResponse paymentMultiStepResponse = null;
        PaymentInformationStatusCodeEntity societeGeneralePaymentStatus =
                paymentResponse.getPayment().getPaymentInformationStatus();
        switch (societeGeneralePaymentStatus) {
            case ACCP:
                if (nextStep.equals(AuthenticationStepConstants.STEP_FINALIZE)) {
                    logger.error(
                            "Payment confirmation failed, psuAuthenticationStatus={}",
                            societeGeneralePaymentStatus);
                    throw new PaymentAuthenticationException(
                            UserMessage.PAYMENT_CONFIRMATION_FAILED.getKey().get(),
                            new PaymentRejectedException());
                }
            case PDNG:
            case ACSC:
            case ACSP:
                paymentMultiStepResponse =
                        new PaymentMultiStepResponse(
                                paymentResponse.toTinkPaymentResponse(),
                                nextStep,
                                new ArrayList<>());
                break;
            case ACTC:
            case ACWC:
            case ACWP:
            case PART:
            case RCVD:
                logger.error(
                        "PSU Authentication failed, psuAuthenticationStatus={}",
                        societeGeneralePaymentStatus);
                throw new PaymentAuthenticationException(
                        UserMessage.PAYMENT_AUTHENTICATION_FAILED.getKey().get(),
                        new PaymentRejectedException());
            case RJCT:
                handleReject(paymentResponse.getPayment().getStatusReasonInformation());
                break;
            case CANC:
                handleCancelled(paymentResponse.getPayment().getStatusReasonInformation());
                break;
            default:
                logger.error(
                        "Payment failed. Invalid Payment status returned by Societe Generale Bank,Status={}",
                        societeGeneralePaymentStatus);
                throw new PaymentException(UserMessage.PAYMENT_FAILED.getKey().get());
        }
        return paymentMultiStepResponse;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "Cancel not implemented for " + this.getClass().getName());
    }

    private CreatePaymentRequest buildPaymentRequest(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        BeneficiaryEntity beneficiary = BeneficiaryEntity.of(paymentRequest);
        List<CreditTransferTransactionEntity> creditTransferTransaction =
                CreditTransferTransactionEntity.of(paymentRequest);
        SupplementaryDataEntity supplementaryData =
                SupplementaryDataEntity.of(paymentRequest, createRedirectUrl().toString());

        return new CreatePaymentRequest.Builder()
                .withPaymentInformationId(UUID.randomUUID().toString())
                .withCreationDateTime(getCreationDate())
                .withNumberOfTransactions(
                        SocieteGeneraleConstants.FormValues.NUMBER_OF_TRANSACTIONS)
                .withInitiatingParty(
                        new PartyIdentificationEntity(
                                SocieteGeneraleConstants.FormValues
                                        .PAYMENT_INITIATION_DEFAULT_NAME))
                .withPaymentTypeInformation(
                        new PaymentTypeInformationEntity(
                                SocieteGeneraleConstants.PaymentTypeInformation.SERVICE_LEVEL))
                .withDebtorAccount(
                        new DebtorAccountEntity(
                                Optional.ofNullable(payment.getDebtor())
                                        .map(Debtor::getAccountNumber)
                                        .orElse(null)))
                .withBeneficiary(beneficiary)
                .withChargeBearer(SocieteGeneraleConstants.FormValues.CHARGE_BEARER_SLEV)
                .withRequestedExecutionDate(
                        calculateExecutionDate(
                                Optional.ofNullable(payment.getExecutionDate())
                                        .orElse(LocalDate.now(DEFAULT_ZONE_ID))))
                .withCreditTransferTransaction(creditTransferTransaction)
                .withSupplementaryData(supplementaryData)
                .build();
    }

    public String calculateExecutionDate(LocalDate localDate) {

        if (dateHelper.checkIfToday(localDate)
                && dateHelper.calculateIfWithinCutOffTime(
                        ZonedDateTime.now(DEFAULT_ZONE_ID), 17, 30, 900)) {
            // Due to bank cut-off time a transfer initiated (and validated by the customer) before
            // 17h30 must be confirmed (with a POST / confirmation) before 17h30.
            // Transfers b/w 17:15 & 17:30 will be moved to next day.
            return ZonedDateTime.of(localDate.plusDays(1), LocalTime.of(1, 0, 0), DEFAULT_ZONE_ID)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        // excexutionDate should not be same as creationDate due to bank limitation,so adding 1
        // minute will resolve this issue.
        return ZonedDateTime.of(
                        localDate, LocalTime.now(DEFAULT_ZONE_ID).plusMinutes(1), DEFAULT_ZONE_ID)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private void handleReject(StatusReasonInformationEntity rejectStatus)
            throws PaymentRejectedException {
        String error = StatusReasonInformationEntity.mapRejectStatusToError(rejectStatus);
        logger.error("Payment Rejected by the bank with error ={}", error);
        throw new PaymentRejectedException(UserMessage.PAYMENT_REJECTED.getKey().get());
    }

    private void handleCancelled(StatusReasonInformationEntity rejectStatus)
            throws PaymentAuthenticationException {
        String error = StatusReasonInformationEntity.mapRejectStatusToError(rejectStatus);
        logger.error("Authorisation of payment was cancelled with bank status={}", error);
        throw new PaymentAuthenticationException(
                UserMessage.PAYMENT_CANCELLED.getKey().get(), new PaymentCancelledException());
    }

    private GetPaymentResponse fetchPaymentStatus(String paymentId) {
        return apiClient.getPaymentStatus(paymentId);
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    private void waitForSupplementalInformation() {
        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(), 9L, TimeUnit.MINUTES);
    }

    private URL createRedirectUrl() {
        return new URL(redirectUrl)
                .queryParam(
                        SocieteGeneraleConstants.QueryKeys.STATE,
                        strongAuthenticationState.getState())
                .queryParam(
                        SocieteGeneraleConstants.QueryKeys.CODE,
                        SocieteGeneraleConstants.QueryValues.CODE);
    }

    private String getCreationDate() {
        return ZonedDateTime.now(DEFAULT_ZONE_ID).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private enum UserMessage implements LocalizableEnum {
        PAYMENT_FAILED(new LocalizableKey("Payment failed.")),
        PAYMENT_AUTHENTICATION_FAILED(new LocalizableKey("Payment authentication failed.")),
        PAYMENT_AUTHORIZATION_FAILED(new LocalizableKey("Payment authorization failed.")),
        PAYMENT_REJECTED(new LocalizableKey("The payment was rejected by the bank.")),
        PAYMENT_CANCELLED(new LocalizableKey("The payment was cancelled by the user.")),
        PAYMENT_CONFIRMATION_FAILED(
                new LocalizableKey("An error occurred while confirming the payment."));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }
}
