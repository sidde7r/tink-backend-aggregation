package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.CommerzBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@Slf4j
public class CommerzBankPaymentExecutor extends Xs2aDevelopersPaymentExecutor {

    private static final int RETRY_ATTEMPTS = 270;
    private static final int SLEEP_TIME_SECOND = 2;

    public CommerzBankPaymentExecutor(
            Xs2aDevelopersApiClient apiClient,
            ThirdPartyAppAuthenticationController controller,
            Credentials credentials,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            CommerzBankPaymentAuthenticator paymentAuthenticator) {
        super(apiClient, controller, credentials, persistentStorage);
        this.sessionStorage = sessionStorage;
        this.authenticator = paymentAuthenticator;
    }

    private final SessionStorage sessionStorage;
    private final CommerzBankPaymentAuthenticator authenticator;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        CreatePaymentResponse createPaymentResponse = getPaymentResponse(paymentRequest);
        persistentStorage.put(StorageKeys.PAYMENT_ID, createPaymentResponse.getPaymentId());
        sessionStorage.put("sca-links", createPaymentResponse.getLinks());

        return createPaymentResponse.toTinkPayment();
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        final String paymentId = payment.getUniqueId();
        return apiClient.getPayment(paymentId).toTinkPayment(paymentId);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        if (AuthenticationStepConstants.STEP_INIT.equals(paymentMultiStepRequest.getStep())) {
            authorizePayment();
        }

        return checkStatus(paymentMultiStepRequest);
    }

    private void authorizePayment() {
        LinksEntity scaLinks =
                sessionStorage
                        .get("sca-links", LinksEntity.class)
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.MISSING_SCA_URL));
        authenticator.authenticatePayment(scaLinks);
        sign();
    }

    protected PaymentMultiStepResponse checkStatus(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String paymentId = persistentStorage.get(StorageKeys.PAYMENT_ID);
        PaymentStatus paymentStatus = poll(paymentId);

        paymentMultiStepRequest.getPayment().setStatus(paymentStatus);

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest, AuthenticationStepConstants.STEP_FINALIZE);
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
        return new PaymentListResponse(
                paymentListRequest.getPaymentRequestList().stream()
                        .map(paymentRequest -> new PaymentResponse(paymentRequest.getPayment()))
                        .collect(Collectors.toList()));
    }

    private PaymentStatus poll(String paymentId) throws PaymentException {
        Retryer<PaymentStatus> retryer = getPaymentStatusRetryer();
        PaymentStatus paymentStatus;
        log.info(
                "Start to Get Payment Status every {} Seconds for a total of {} times.",
                SLEEP_TIME_SECOND,
                RETRY_ATTEMPTS);
        try {
            paymentStatus =
                    retryer.call(
                            () ->
                                    apiClient
                                            .getPayment(paymentId)
                                            .toTinkPayment(paymentId)
                                            .getPayment()
                                            .getStatus());

        } catch (ExecutionException | RetryException e) {
            throw new PaymentRejectedException();
        }

        return checkPaymentStatus(paymentStatus);
    }

    protected PaymentStatus checkPaymentStatus(PaymentStatus paymentStatus)
            throws PaymentException {
        switch (paymentStatus) {
            case SIGNED:
            case PAID:
                return paymentStatus;
            case PENDING:
            case REJECTED:
                throw new PaymentRejectedException();
            case CANCELLED:
                throw new PaymentCancelledException();
            default:
                log.error("Payment in unexpected status after signing: {}", paymentStatus);
                throw new PaymentAuthorizationException();
        }
    }

    protected Retryer<PaymentStatus> getPaymentStatusRetryer() {
        return RetryerBuilder.<PaymentStatus>newBuilder()
                .retryIfResult(status -> status == PaymentStatus.PENDING)
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME_SECOND, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }
}
