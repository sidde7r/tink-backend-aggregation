package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.executor.payment;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.PaymentStatus;

@Slf4j
public class N26Xs2aPaymentExecutor extends Xs2aDevelopersPaymentExecutor {
    private static final int SLEEP_TIME_SECOND = 2;
    private static final int RETRY_ATTEMPTS = 270;

    public N26Xs2aPaymentExecutor(
            Xs2aDevelopersApiClient apiClient,
            ThirdPartyAppAuthenticationController controller,
            Credentials credentials,
            PersistentStorage persistentStorage) {
        super(apiClient, controller, credentials, persistentStorage);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        fetchToken();
        return super.create(paymentRequest);
    }

    private void fetchToken() {
        // Authenticator will handle the token fetch
        super.sign();
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String paymentId = persistentStorage.get(Xs2aDevelopersConstants.StorageKeys.PAYMENT_ID);

        PaymentStatus finalStatus = poll(paymentId);
        paymentMultiStepRequest.getPayment().setStatus(finalStatus);

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest,
                AuthenticationStepConstants.STEP_FINALIZE,
                new ArrayList<>());
    }

    private PaymentStatus poll(String paymentId)
            throws PaymentRejectedException, PaymentAuthorizationException {
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
            log.warn("Retryer couldn't get payment status");
            throw new PaymentRejectedException("Retryer couldn't get payment status");
        }

        if (paymentStatus == PaymentStatus.PENDING) {
            throw new PaymentAuthorizationException();
        }

        if (paymentStatus != PaymentStatus.SIGNED) {
            throw new PaymentRejectedException("Unexpected payment status: " + paymentStatus);
        }

        return paymentStatus;
    }

    private Retryer<PaymentStatus> getPaymentStatusRetryer() {
        return RetryerBuilder.<PaymentStatus>newBuilder()
                .retryIfResult(status -> status == PaymentStatus.PENDING)
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME_SECOND, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }
}
