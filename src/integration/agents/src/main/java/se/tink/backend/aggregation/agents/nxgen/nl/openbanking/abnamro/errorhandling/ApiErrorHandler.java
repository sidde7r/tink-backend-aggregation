package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.errorhandling;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.Errors;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public final class ApiErrorHandler {
    private static final int MAX_TIME_WAIT_IN_S = 15;
    private static final int MAX_NUM_OF_ATTEMPTS = 5;

    private static final int TOO_MANY_REQUESTS = 429;
    private static final List<Integer> RETRYABLE_STATUSES =
            Arrays.asList(
                    HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    HttpStatus.SC_SERVICE_UNAVAILABLE,
                    HttpStatus.SC_BAD_GATEWAY);

    public enum RequestType {
        GET,
        POST
    }

    public static <T> T callWithErrorHandling(
            RequestBuilder requestBuilder, Class<T> clazz, RequestType requestType) {
        try {
            return callApi(requestBuilder, clazz, requestType);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
        } catch (ExecutionException | RetryException e) {
            throw new RuntimeException("[ABN] Something bad happened when calling bank API", e);
        }
        // should not reach here
        return null;
    }

    private static <T> T callApi(
            RequestBuilder requestBuilder, Class<T> clazz, RequestType requestType)
            throws ExecutionException, RetryException {
        Retryer<T> retryer = prepareRetryer();
        if (RequestType.GET == requestType) {
            return retryer.call(() -> requestBuilder.get(clazz));
        } else if (RequestType.POST == requestType) {
            return retryer.call(() -> requestBuilder.post(clazz));
        } else {
            throw new UnsupportedOperationException("Such request type is not supported");
        }
    }

    private static <T> Retryer<T> prepareRetryer() {
        return RetryerBuilder.<T>newBuilder()
                .retryIfException(
                        e ->
                                e instanceof HttpResponseException
                                        && (RETRYABLE_STATUSES.contains(
                                                ((HttpResponseException) e)
                                                        .getResponse()
                                                        .getStatus())))
                .withWaitStrategy(
                        WaitStrategies.exponentialWait(MAX_TIME_WAIT_IN_S, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(MAX_NUM_OF_ATTEMPTS))
                .withRetryListener(getListener())
                .build();
    }

    private static RetryListener getListener() {
        return new RetryListener() {
            @Override
            public <V> void onRetry(Attempt<V> attempt) {
                if (attempt.hasException()) {
                    log.warn("[ABN] Retryable exception happened, retrying...");
                }
            }
        };
    }

    private static void handleHttpResponseException(HttpResponseException e) {
        log.error("[ABN] HttpResponseException occurred - attempting to handle...");
        ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
        Errors error = errorResponse.getErrors().get(0);
        handleBankSideIssues(e, error);
        log.error("[ABN] Unhandled issue - please add handling!");
        throw e;
    }

    private static void handleBankSideIssues(HttpResponseException e, Errors error) {
        if (RETRYABLE_STATUSES.contains(e.getResponse().getStatus())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(error.getMessage());
        } else if (TOO_MANY_REQUESTS == e.getResponse().getStatus()) {
            throw BankServiceError.ACCESS_EXCEEDED.exception(error.getMessage());
        }
    }
}
