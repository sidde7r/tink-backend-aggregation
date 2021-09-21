package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment;

import static java.util.concurrent.TimeUnit.SECONDS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums.CurrentStep.WIZARD_FINISH_STEP;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums.CurrentStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.GetSessionsResponse;

public class SessionStatusRetryer {
    private static final long SLEEP_TIME_SECOND = 1;
    private static final int RETRY_ATTEMPTS = 20;

    public boolean callUntilSessionStatusIsNotFinished(
            Callable<GetSessionsResponse> sessionsResponse)
            throws ExecutionException, RetryException {
        return RetryerBuilder.<GetSessionsResponse>newBuilder()
                .retryIfResult(this::isWidgetSessionNotOver)
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME_SECOND, SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build()
                .call(sessionsResponse)
                .isFinished();
    }

    private boolean isWidgetSessionNotOver(GetSessionsResponse sessionsResponse) {
        return sessionsResponse.isFinished()
                && WIZARD_FINISH_STEP.equals(
                        CurrentStep.fromString(sessionsResponse.getCurrentStep()));
    }
}
