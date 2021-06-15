package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.payment.executor;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.BuddybankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.UnicreditPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public class BuddybankPaymentController extends PaymentController {

    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;
    private final BuddybankApiClient apiClient;

    public BuddybankPaymentController(
            UnicreditPaymentExecutor paymentExecutor, UnicreditBaseApiClient apiClient) {
        super(paymentExecutor, paymentExecutor);

        this.apiClient = (BuddybankApiClient) apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        PaymentResponse paymentResponse = super.create(paymentRequest);

        Retryer<FetchPaymentStatusResponse> consentStatusRetryer = getPaymentStatusRetryer();

        try {
            consentStatusRetryer.call(() -> apiClient.fetchPaymentStatus(paymentRequest));

        } catch (RetryException e) {
            throw new IllegalStateException("Authorization status error!");
        } catch (ExecutionException e) {
            throw new IllegalStateException("Authorization api error!");
        }

        return paymentResponse;
    }

    private Retryer<FetchPaymentStatusResponse> getPaymentStatusRetryer() {
        return RetryerBuilder.<FetchPaymentStatusResponse>newBuilder()
                .retryIfResult(status -> !Objects.isNull(status) && !status.isAuthorizedPayment())
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        return super.sign(paymentMultiStepRequest);
    }
}
