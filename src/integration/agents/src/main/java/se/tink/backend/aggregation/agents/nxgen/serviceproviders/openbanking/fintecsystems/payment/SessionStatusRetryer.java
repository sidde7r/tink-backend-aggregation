package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.FinTechSystemsSession;

public class SessionStatusRetryer {
    private static final long SLEEP_TIME_SECOND = 3;
    private static final int RETRY_MINUTES = 9;

    public FinTechSystemsSession callUntilSessionStatusIsNotFinished(
            Callable<FinTechSystemsSession> sessionsResponse)
            throws ExecutionException, RetryException {
        return RetryerBuilder.<FinTechSystemsSession>newBuilder()
                .retryIfResult(this::isWidgetSessionNotOver)
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME_SECOND, SECONDS))
                .withStopStrategy(StopStrategies.stopAfterDelay(RETRY_MINUTES, MINUTES))
                .build()
                .call(sessionsResponse);
    }

    private boolean isWidgetSessionNotOver(FinTechSystemsSession sessionsResponse) {
        return !sessionsResponse.isFinished();
    }
}
