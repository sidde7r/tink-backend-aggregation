package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static se.tink.libraries.payment.enums.PaymentStatus.PENDING;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public class UnicreditApiClientRetryer {

    private static final long SLEEP_TIME_SECOND = 1;
    private static final int RETRY_ATTEMPTS = 540;

    public PaymentResponse callUntilPaymentStatusIsNotPending(
            Callable<PaymentResponse> fetchPaymentStatus)
            throws ExecutionException, RetryException {
        Retryer<PaymentResponse> retryer = getPaymentStatusRetryer();
        return retryer.call(fetchPaymentStatus);
    }

    private Retryer<PaymentResponse> getPaymentStatusRetryer() {
        return RetryerBuilder.<PaymentResponse>newBuilder()
                .retryIfResult(this::paymentResponseStatusIsPending)
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME_SECOND, SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }

    private boolean paymentResponseStatusIsPending(PaymentResponse paymentResponse) {
        return paymentResponse != null && paymentResponse.isStatus(PENDING);
    }
}
