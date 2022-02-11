package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingErrorMapper;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
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
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Slf4j
public class FrOpenBankingPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    static final String PAYMENT_POST_SIGN_STATE = "payment_post_sign_state";
    static final String PAYMENT_AUTHORIZATION_URL = "payment_authorization_url";
    private static final String STATE = "state";

    private static final long WAIT_FOR_MINUTES = 9L;
    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    private final FrOpenBankingPaymentApiClient apiClient;
    private final String redirectUrl;
    private final SessionStorage sessionStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final FrOpenBankingPaymentDatePolicy frOpenBankingPaymentDatePolicy;
    private final FrOpenBankingRequestValidator frOpenBankingRequestValidator;

    public FrOpenBankingPaymentExecutor(
            FrOpenBankingPaymentApiClient apiClient,
            String redirectUrl,
            SessionStorage sessionStorage,
            StrongAuthenticationState strongAuthenticationState,
            SupplementalInformationHelper supplementalInformationHelper,
            FrOpenBankingPaymentDatePolicy frOpenBankingPaymentDatePolicy,
            FrOpenBankingRequestValidator frOpenBankingRequestValidator) {
        this.apiClient = apiClient;
        this.redirectUrl = redirectUrl;
        this.sessionStorage = sessionStorage;
        this.strongAuthenticationState = strongAuthenticationState;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.frOpenBankingPaymentDatePolicy = frOpenBankingPaymentDatePolicy;
        this.frOpenBankingRequestValidator = frOpenBankingRequestValidator;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        apiClient.fetchToken();

        Optional<PaymentException> paymentException =
                frOpenBankingRequestValidator.validateRequestBody(paymentRequest);
        if (paymentException.isPresent()) {
            throw paymentException.get();
        }

        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);
        Optional.ofNullable(paymentRequest.getPayment().getDebtor())
                .map(Debtor::getAccountIdentifier)
                .ifPresent(
                        accountIdentifier ->
                                log.info(
                                        "Debtor Account AccountIdentifier validate: "
                                                + accountIdentifier.isValid()));
        Payment payment = paymentRequest.getPayment();

        PaymentType paymentType = PaymentType.SEPA;
        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);
        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withPaymentType(paymentType)
                        .withAmount(amount)
                        .withCreditorAccount(creditor)
                        .withCreditorName(new CreditorEntity(payment.getCreditor().getName()))
                        .withDebtorAccount(debtor)
                        .withExecutionDate(
                                frOpenBankingPaymentDatePolicy.getExecutionDateWithBankTimeZone(
                                        payment))
                        .withCreationDateTime(frOpenBankingPaymentDatePolicy.getCreationDate())
                        .withRedirectUrl(
                                new URL(redirectUrl)
                                        .queryParam(STATE, strongAuthenticationState.getState()))
                        .withRemittanceInformation(remittanceInformation.getValue())
                        .withPaymentScheme(payment.getPaymentScheme())
                        .build();

        CreatePaymentResponse paymentResponse = apiClient.createPayment(createPaymentRequest);

        String authorizationUrl = paymentResponse.getLinks().getAuthorizationUrl();
        sessionStorage.put(PAYMENT_AUTHORIZATION_URL, authorizationUrl);

        String paymentId = apiClient.findPaymentId(authorizationUrl);
        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(paymentId)
                        .withType(paymentType)
                        .withCurrency(amount.getCurrency())
                        .withExactCurrencyAmount(amount.toTinkAmount())
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .build());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(paymentRequest.getPayment().getUniqueId())
                .toTinkPaymentResponse(paymentRequest.getPayment().getUniqueId());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String nextStep;
        Payment payment = paymentMultiStepRequest.getPayment();

        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                String authorizationUrl = sessionStorage.get(PAYMENT_AUTHORIZATION_URL);
                openThirdPartyApp(new URL(authorizationUrl));
                nextStep = PAYMENT_POST_SIGN_STATE;
                break;
            case PAYMENT_POST_SIGN_STATE:
                payment = verifyStatus(payment.getUniqueId()).getPayment();
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new PaymentException(
                        "Unknown step " + paymentMultiStepRequest.getStep() + " for payment sign.",
                        InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }

        return new PaymentMultiStepResponse(payment, nextStep);
    }

    private void openThirdPartyApp(URL authorizationUrl) throws PaymentAuthorizationException {
        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizationUrl));
        this.supplementalInformationHelper
                .waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES)
                .orElseThrow(
                        () ->
                                new PaymentAuthorizationException(
                                        "SCA time-out.",
                                        InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT,
                                        ThirdPartyAppError.TIMED_OUT.exception()));
    }

    private PaymentResponse verifyStatus(String paymentId) throws PaymentException {
        Retryer<GetPaymentResponse> paymentResponseRetryer = getPaymentResponseRetryer();

        GetPaymentResponse getPaymentResponse;
        log.info(
                "Start to Get Payment Status every {} Seconds for a total of {} times.",
                SLEEP_TIME,
                RETRY_ATTEMPTS);
        try {
            getPaymentResponse = paymentResponseRetryer.call(() -> apiClient.getPayment(paymentId));

        } catch (ExecutionException | RetryException e) {
            log.warn("Payment failed, couldn't fetch payment status", e);
            throw new PaymentRejectedException(
                    "Payment failed, couldn't fetch payment status",
                    InternalStatus.PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION);
        }

        if (getPaymentResponse.getPaymentStatus() == PaymentStatus.PENDING) {
            throw new PaymentRejectedException();
        }

        if (getPaymentResponse.getPaymentStatus() != PaymentStatus.SIGNED) {
            throw FrOpenBankingErrorMapper.mapToError(
                    getPaymentResponse.getStatusReasonInformation());
        }

        return getPaymentResponse.toTinkPaymentResponse(paymentId);
    }

    private Retryer<GetPaymentResponse> getPaymentResponseRetryer() {
        return RetryerBuilder.<GetPaymentResponse>newBuilder()
                .retryIfResult(
                        paymentResponse ->
                                paymentResponse == null
                                        || paymentResponse.getPaymentStatus()
                                                == PaymentStatus.PENDING)
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
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
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }
}
