package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.enums.BnpParibasPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.rpc.CreatePaymentResponse;
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
import se.tink.libraries.payment.rpc.Payment;

@Slf4j
public class BnpParibasPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final String BNP_PAYMENT_POST_SIGN_STATE = "bnp_payment_post_sign_state";
    private static final long WAIT_FOR_MINUTES = 9L;
    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    private final BnpParibasApiBaseClient apiClient;
    private final BnpParibasConfiguration configuration;
    private final SessionStorage sessionStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public BnpParibasPaymentExecutor(
            BnpParibasApiBaseClient apiClient,
            BnpParibasConfiguration configuration,
            SessionStorage sessionStorage,
            StrongAuthenticationState strongAuthenticationState,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.sessionStorage = sessionStorage;
        this.strongAuthenticationState = strongAuthenticationState;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        apiClient.fetchToken();

        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);

        Payment payment = paymentRequest.getPayment();

        BnpParibasPaymentType paymentType = BnpParibasPaymentType.SEPA;

        LocalDate executionDate =
                Optional.ofNullable(payment.getExecutionDate()).orElse(LocalDate.now());

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withPaymentType(paymentType)
                        .withAmount(amount)
                        .withCreditorAccount(creditor)
                        .withCreditorName(new CreditorEntity(payment.getCreditor().getName()))
                        .withDebtorAccount(debtor)
                        .withExecutionDate(executionDate)
                        .withCreationDateTime(LocalDateTime.now())
                        .withRedirectUrl(
                                new URL(configuration.getRedirectUrl())
                                        .queryParam(
                                                QueryKeys.STATE,
                                                strongAuthenticationState.getState()))
                        .withRemittanceInformation(payment.getReference().getValue())
                        .build();

        CreatePaymentResponse paymentResponse = apiClient.createPayment(createPaymentRequest);

        sessionStorage.put(
                StorageKeys.PAYMENT_AUTHORIZATION_URL,
                paymentResponse.getLinks().getAuthorizationUrl());

        return paymentResponse.toTinkPaymentResponse(creditor, debtor, amount, paymentType);
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
                String authorizationUrl =
                        Optional.ofNullable(
                                        sessionStorage.get(StorageKeys.PAYMENT_AUTHORIZATION_URL))
                                .orElseThrow(
                                        () ->
                                                new PaymentAuthenticationException(
                                                        "Payment authentication failed. There is no authorization url!",
                                                        new PaymentRejectedException()));
                openThirdPartyApp(new URL(authorizationUrl));
                nextStep = BNP_PAYMENT_POST_SIGN_STATE;
                break;
            case BNP_PAYMENT_POST_SIGN_STATE:
                PaymentStatus paymentStatus = getAndVerifyStatus(payment.getUniqueId());
                payment.setStatus(paymentStatus);
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new PaymentException(
                        "Unknown step " + paymentMultiStepRequest.getStep() + " for payment sign.");
        }

        return new PaymentMultiStepResponse(payment, nextStep, Collections.emptyList());
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl));
        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(), WAIT_FOR_MINUTES, TimeUnit.MINUTES);
    }

    private PaymentStatus getAndVerifyStatus(String paymentId) throws PaymentException {
        Retryer<PaymentStatus> paymentStatusRetryer = getPaymentStatusRetryer();

        PaymentStatus paymentStatus;
        log.info(
                "Start to Get Payment Status every {} Seconds for a total of {} times.",
                SLEEP_TIME,
                RETRY_ATTEMPTS);
        try {
            paymentStatus =
                    paymentStatusRetryer.call(
                            () -> apiClient.getPayment(paymentId).getPaymentStatus());

        } catch (ExecutionException | RetryException e) {
            log.warn("Payment failed, couldn't fetch payment status");
            throw new PaymentRejectedException("Payment failed, couldn't fetch payment status");
        }

        if (paymentStatus == PaymentStatus.PENDING) {
            throw new PaymentAuthenticationException(
                    "Payment authentication failed.", new PaymentRejectedException());
        }

        if (paymentStatus != PaymentStatus.SIGNED) {
            throw new PaymentRejectedException("Unexpected payment status: " + paymentStatus);
        }

        return paymentStatus;
    }

    private Retryer<PaymentStatus> getPaymentStatusRetryer() {
        return RetryerBuilder.<PaymentStatus>newBuilder()
                .retryIfResult(status -> status == PaymentStatus.PENDING)
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
