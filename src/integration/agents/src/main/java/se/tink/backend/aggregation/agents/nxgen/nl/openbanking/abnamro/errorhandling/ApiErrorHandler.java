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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.Errors;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.OAuth2ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class ApiErrorHandler {
    public static final String TOKEN_EXPIRED_CODE = "ERR_2100_001";

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
        } catch (ExecutionException | RetryException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpResponseException) {
                handleHttpResponseException((HttpResponseException) cause);
            }
            throw new RuntimeException("[ABN] Something bad happened when calling bank API", e);
        }
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

    static <T> Retryer<T> prepareRetryer() {
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
        tryHandleOAuth2ErrorResponse(e);
        tryHandleErrorResponse(e);
        log.error("[ABN] Unhandled issue - please add handling!");
        throw e;
    }

    private static void tryHandleOAuth2ErrorResponse(HttpResponseException e) {
        OAuth2ErrorResponse oAuth2ErrorResponse =
                e.getResponse().getBody(OAuth2ErrorResponse.class);
        String error = oAuth2ErrorResponse.getError();
        if (StringUtils.isNotBlank(error)) {
            if ("invalid_grant".equals(error)) {
                throw SessionError.CONSENT_INVALID.exception();
            } else {
                log.error("[ABN] Unhandled issue in OAuth2 communication - please add handling!");
            }
        }
    }

    private static void tryHandleErrorResponse(HttpResponseException e) {
        ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
        if (CollectionUtils.isNotEmpty(errorResponse.getErrors())) {
            Errors error = errorResponse.getErrors().get(0);
            handleBankSideIssues(e, error);
            handleCustomerIssues(e, error);
        }
    }

    private static void handleBankSideIssues(HttpResponseException e, Errors error) {
        if (RETRYABLE_STATUSES.contains(e.getResponse().getStatus())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(error.getMessage());
        } else if (TOO_MANY_REQUESTS == e.getResponse().getStatus()) {
            throw BankServiceError.ACCESS_EXCEEDED.exception(error.getMessage());
        }
    }

    private static void handleCustomerIssues(HttpResponseException e, Errors error) {
        if (HttpStatus.SC_UNAUTHORIZED == e.getResponse().getStatus()
                && TOKEN_EXPIRED_CODE.equals(error.getCode())) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
