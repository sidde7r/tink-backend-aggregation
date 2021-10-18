package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.PaymentSteps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.ConfirmationResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestCreation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentInformationStatusCodeEntity;
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
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@Slf4j
public class CmcicPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    private final CmcicApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final String redirectUrl;
    private final List<PaymentResponse> paymentResponses = new ArrayList<>();
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final CmcicPaymentRequestFactory paymentRequestFactory;
    private final CmcicPaymentResponseMapper responseMapper;
    private final Retryer<HalPaymentRequestEntity> retryer;

    public CmcicPaymentExecutor(
            CmcicApiClient apiClient,
            SessionStorage sessionStorage,
            AgentConfiguration<CmcicConfiguration> agentConfiguration,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            CmcicPaymentRequestFactory paymentRequestFactory,
            CmcicPaymentResponseMapper responseMapper) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
        this.paymentRequestFactory = paymentRequestFactory;
        this.responseMapper = responseMapper;
        this.retryer = createRetryer();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        apiClient.fetchPisOauthToken();
        sessionStorage.put(StorageKeys.STATE, strongAuthenticationState.getState());
        String callbackUrl =
                redirectUrl + Urls.SUCCESS_REPORT_PATH + sessionStorage.get(StorageKeys.STATE);
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
        String paymentId = getPaymentId(sessionStorage.get(StorageKeys.AUTH_URL));
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                openThirdPartyApp(new URL(sessionStorage.get(StorageKeys.AUTH_URL)));
                waitForSupplementalInformation();
                paymentMultiStepResponse =
                        new PaymentMultiStepResponse(payment, PaymentSteps.POST_SIGN_STEP);
                break;
            case PaymentSteps.POST_SIGN_STEP:
                try {
                    HalPaymentRequestEntity getPaymentResponse =
                            retryer.call(() -> apiClient.fetchPayment(paymentId));
                    paymentMultiStepResponse =
                            handleSignedPayment(
                                    getPaymentResponse, PaymentSteps.CONFIRM_PAYMENT_STEP);
                } catch (ExecutionException | RetryException e) {
                    log.warn("Payment failed, couldn't fetch payment status");
                    throw new PaymentRejectedException(
                            "Payment failed, couldn't fetch payment status",
                            InternalStatus.PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION);
                }
                break;
            case PaymentSteps.CONFIRM_PAYMENT_STEP:
                ConfirmationResourceEntity confirmationResourceEntity =
                        new ConfirmationResourceEntity();
                confirmationResourceEntity.setPsuAuthenticationFactor(
                        sessionStorage.get(StorageKeys.AUTH_FACTOR));
                HalPaymentRequestEntity paymentConfirmResponse =
                        apiClient.confirmPayment(paymentId, confirmationResourceEntity);
                paymentMultiStepResponse =
                        handleSignedPayment(
                                paymentConfirmResponse, AuthenticationStepConstants.STEP_FINALIZE);
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

        sessionStorage.put(StorageKeys.AUTH_URL, authorizeUrl);
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
        PaymentInformationStatusCodeEntity paymentStatus =
                paymentRequest.getPaymentInformationStatusCode();
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

    private String getPaymentId(String authorizationUrl) throws PaymentException {
        int index = authorizationUrl.lastIndexOf('=');
        if (index < 0) {
            log.error("Payment failed due to missing paymentId");
            throw new PaymentException(
                    TransferExecutionException.EndUserMessage.GENERIC_PAYMENT_ERROR_MESSAGE
                            .getKey()
                            .get(),
                    InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
        return authorizationUrl.substring(index + 1);
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

        // Query parameters can be case insesitive returned by bank,this is to take care of that
        // situation and we avoid failing the payment.
        Map<String, String> caseInsensitiveCallbackData = new CaseInsensitiveMap<>(callbackData);

        String psuAuthenticationFactor =
                caseInsensitiveCallbackData.get(CmcicConstants.QueryKeys.PSU_AUTHENTICATION_FACTOR);
        if (Strings.isNullOrEmpty(psuAuthenticationFactor)) {
            handelAuthFactorError();
        }
        sessionStorage.put(StorageKeys.AUTH_FACTOR, psuAuthenticationFactor);
    }

    private void handelAuthFactorError() throws PaymentRejectedException {
        log.error("Payment authorization failed. There is no psuAuthenticationFactor!");
        throw new PaymentRejectedException();
    }
}
