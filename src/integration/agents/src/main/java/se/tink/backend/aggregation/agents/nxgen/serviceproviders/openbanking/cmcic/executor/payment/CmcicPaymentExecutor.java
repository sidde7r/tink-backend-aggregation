package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.PaymentSteps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicRepository;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback.CmcicCallbackInterpreter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestCreation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentInformationStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentRequestResourceCreationLinks;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentRequestResourceCreationLinksConsentApproval;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentRequestResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.StatusReasonInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingErrorMapper;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@Slf4j
public class CmcicPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    private final CmcicApiClient apiClient;
    private final CmcicRepository cmcicRepository;
    private final String redirectUrl;
    private final List<PaymentResponse> paymentResponses = new ArrayList<>();
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final CmcicPaymentRequestFactory paymentRequestFactory;
    private final CmcicPaymentResponseMapper responseMapper;
    private final Retryer<HalPaymentRequestEntity> retryer;
    private final CmcicCallbackInterpreter callbackHandler;

    public CmcicPaymentExecutor(
            CmcicApiClient apiClient,
            CmcicRepository cmcicRepository,
            AgentConfiguration<CmcicConfiguration> agentConfiguration,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            CmcicPaymentRequestFactory paymentRequestFactory,
            CmcicPaymentResponseMapper responseMapper) {
        this.apiClient = apiClient;
        this.cmcicRepository = cmcicRepository;
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
        this.paymentRequestFactory = paymentRequestFactory;
        this.responseMapper = responseMapper;
        this.retryer = createRetryer();
        this.callbackHandler = new CmcicCallbackInterpreter(cmcicRepository);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        apiClient.fetchPisOauthToken();
        cmcicRepository.storeState(strongAuthenticationState.getState());
        String callbackUrl = redirectUrl + Urls.SUCCESS_REPORT_PATH + cmcicRepository.getState();
        PaymentRequestResourceEntity paymentRequestResourceEntity =
                paymentRequestFactory.buildPaymentRequest(paymentRequest.getPayment(), callbackUrl);
        HalPaymentRequestCreation paymentRequestCreation =
                apiClient.makePayment(paymentRequestResourceEntity);

        saveAuthorizeUrl(paymentRequestCreation);
        PaymentResponse response = responseMapper.map(paymentRequestResourceEntity);
        paymentResponses.add(response);

        return response;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        HalPaymentRequestEntity paymentRequestEntity =
                apiClient.fetchPayment(paymentRequest.getPayment().getUniqueId());
        return responseMapper.map(paymentRequestEntity.getPaymentRequest());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentMultiStepResponse paymentMultiStepResponse;
        Payment payment = paymentMultiStepRequest.getPayment();
        String paymentId = cmcicRepository.getPaymentId();
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                paymentMultiStepResponse = processStepInitStep(payment);
                break;
            case PaymentSteps.CONFIRM_PAYMENT_STEP:
                paymentMultiStepResponse = processConfirmPaymentStep(paymentId);
                break;
            case PaymentSteps.POST_CONFIRM_STEP:
                paymentMultiStepResponse = processPostConfirmStep(paymentId);
                break;
            default:
                log.error(
                        "Payment failed due to unknown sign step{} ",
                        paymentMultiStepRequest.getStep());
                throw new PaymentException(
                        TransferExecutionException.EndUserMessage.GENERIC_PAYMENT_ERROR_MESSAGE
                                .getKey()
                                .get(),
                        InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
        return paymentMultiStepResponse;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(paymentResponses);
    }

    private PaymentMultiStepResponse processPostConfirmStep(String paymentId)
            throws PaymentException {
        try {
            HalPaymentRequestEntity getPaymentResponse =
                    retryer.call(() -> apiClient.fetchPayment(paymentId));
            return handleSignedPayment(
                    getPaymentResponse, AuthenticationStepConstants.STEP_FINALIZE);
        } catch (ExecutionException | RetryException e) {
            log.warn("Payment failed, couldn't fetch payment status");
            throw new PaymentRejectedException(
                    "Payment failed, couldn't fetch payment status",
                    InternalStatus.PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION);
        }
    }

    private PaymentMultiStepResponse processStepInitStep(Payment payment) throws PaymentException {
        URL url = new URL(cmcicRepository.getAuthorizationUrl());
        openThirdPartyApp(apiClient.getAuthorizePisURL(url, strongAuthenticationState.getState()));
        waitForSupplementalInformation();
        return new PaymentMultiStepResponse(payment, PaymentSteps.CONFIRM_PAYMENT_STEP);
    }

    private PaymentMultiStepResponse processConfirmPaymentStep(String paymentId)
            throws PaymentException {
        HalPaymentRequestEntity requestEntity = apiClient.fetchPayment(paymentId);
        verifyNotRejected(requestEntity);
        OAuth2Token accessToken =
                apiClient.exchangeCodeForToken(cmcicRepository.getAuthorizationCode());
        cmcicRepository.storePispToken(accessToken);

        HalPaymentRequestEntity paymentConfirmResponse = apiClient.confirmPayment(paymentId);
        return handleSignedPayment(paymentConfirmResponse, PaymentSteps.POST_CONFIRM_STEP);
    }

    private void saveAuthorizeUrl(HalPaymentRequestCreation paymentRequestCreation)
            throws PaymentAuthorizationException {
        String authorizeUrl =
                Optional.ofNullable(paymentRequestCreation)
                        .map(HalPaymentRequestCreation::getLinks)
                        .map(PaymentRequestResourceCreationLinks::getConsentApproval)
                        .map(PaymentRequestResourceCreationLinksConsentApproval::getHref)
                        .orElseThrow(
                                () -> {
                                    log.error(
                                            "Payment authorization failed. There is no authentication url!");
                                    return new PaymentAuthorizationException(
                                            EndUserMessage.PAYMENT_AUTHORIZATION_FAILED
                                                    .getKey()
                                                    .get(),
                                            new PaymentRejectedException());
                                });

        cmcicRepository.storeAuthorizationUrl(authorizeUrl);
    }

    private void verifyNotRejected(HalPaymentRequestEntity requestEntity) throws PaymentException {
        PaymentStatus paymentStatus =
                Optional.ofNullable(requestEntity)
                        .map(HalPaymentRequestEntity::getPaymentRequest)
                        .map(PaymentResponseEntity::getPaymentInformationStatus)
                        .map(PaymentInformationStatusEntity::getPaymentStatus)
                        .orElseThrow(() -> new PaymentException("No payment information status"));

        if (paymentStatus == PaymentStatus.REJECTED) {
            handleReject(requestEntity.getPaymentRequest().getStatusReasonInformation());
        }
    }

    private Retryer<HalPaymentRequestEntity> createRetryer() {
        return RetryerBuilder.<HalPaymentRequestEntity>newBuilder()
                .retryIfResult(
                        paymentResponse -> paymentResponse == null || paymentResponse.isPending())
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }

    private PaymentMultiStepResponse handleSignedPayment(
            HalPaymentRequestEntity paymentResponse, String nextStep) throws PaymentException {
        PaymentMultiStepResponse paymentMultiStepResponse = null;
        PaymentResponseEntity paymentRequest = paymentResponse.getPaymentRequest();
        PaymentInformationStatusEntity paymentStatus = paymentRequest.getPaymentInformationStatus();
        switch (paymentStatus) {
            case ACCP:
                if (nextStep.equals(AuthenticationStepConstants.STEP_FINALIZE)) {
                    log.error(
                            "Payment confirmation failed, psuAuthenticationStatus={}",
                            paymentStatus);
                    throw new PaymentAuthenticationException(
                            TransferExecutionException.EndUserMessage.PAYMENT_CONFIRMATION_FAILED
                                    .getKey()
                                    .get(),
                            new PaymentRejectedException());
                } else {
                    paymentMultiStepResponse =
                            new PaymentMultiStepResponse(
                                    responseMapper.map(paymentRequest), nextStep);
                }
                break;
            case PDNG:
            case ACSC:
            case ACSP:
                paymentMultiStepResponse =
                        new PaymentMultiStepResponse(responseMapper.map(paymentRequest), nextStep);
                break;
            case ACTC:
            case ACWC:
            case ACWP:
            case PART:
            case RCVD:
                log.error("PSU Authentication failed, psuAuthenticationStatus={}", paymentStatus);
                throw new PaymentAuthenticationException(
                        TransferExecutionException.EndUserMessage.PAYMENT_AUTHENTICATION_FAILED
                                .getKey()
                                .get(),
                        new PaymentRejectedException());
            case RJCT:
                handleReject(paymentRequest.getStatusReasonInformation());
                break;
            case CANC:
                handleCancel();
                break;
            default:
                log.error(
                        "Payment failed. Invalid Payment status returned by Societe Generale Bank,Status={}",
                        paymentStatus);
                throw new PaymentException(
                        TransferExecutionException.EndUserMessage.GENERIC_PAYMENT_ERROR_MESSAGE
                                .getKey()
                                .get(),
                        InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
        return paymentMultiStepResponse;
    }

    private void handleReject(StatusReasonInformationEntity rejectStatus) throws PaymentException {
        log.error("Payment Rejected by the bank");
        throw FrOpenBankingErrorMapper.mapToError(rejectStatus.toString());
    }

    private void handleCancel() throws PaymentAuthenticationException {
        log.error("Authorisation of payment was cancelled");
        throw new PaymentAuthenticationException(
                TransferExecutionException.EndUserMessage.PAYMENT_CANCELLED.getKey().get(),
                new PaymentAuthorizationException(InternalStatus.PAYMENT_AUTHORIZATION_CANCELLED));
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    private void waitForSupplementalInformation() throws PaymentException {
        Map<String, String> callbackData =
                this.supplementalInformationHelper
                        .waitForSupplementalInformation(
                                strongAuthenticationState.getSupplementalKey(),
                                9L,
                                TimeUnit.MINUTES)
                        .orElseThrow(
                                () ->
                                        new PaymentAuthorizationException(
                                                "SCA time-out.",
                                                InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT,
                                                ThirdPartyAppError.TIMED_OUT.exception()));

        callbackHandler.interpretCallbackData(callbackData);
    }
}
