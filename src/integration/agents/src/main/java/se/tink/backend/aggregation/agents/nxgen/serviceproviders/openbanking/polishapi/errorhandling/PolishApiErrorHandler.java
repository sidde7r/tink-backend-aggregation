package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrors.DAILY_REQUEST_LIMIT_REACHED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrors.DAYS_EN_90;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrors.DAYS_PL_90;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrors.NOT_IMPLEMENTED;

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
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.dto.responses.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public final class PolishApiErrorHandler {
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

    @SneakyThrows
    public static <T> T callWithErrorHandling(
            RequestBuilder requestBuilder, Class<T> clazz, RequestType requestType) {
        try {
            return callApi(requestBuilder, clazz, requestType);
        } catch (ExecutionException | RetryException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpResponseException) {
                handleHttpResponseException((HttpResponseException) cause);
            } else {
                throw new RuntimeException(
                        "[Polish API] Error handler - Something bad happened when calling bank API",
                        e);
            }
            return null;
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
            throw new UnsupportedOperationException(
                    "[Polish API] Error handler - Such request type is not supported");
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
                    log.warn(
                            "[Polish API] Error handler - Retryable exception happened, retrying...");
                }
            }
        };
    }

    private static void handleHttpResponseException(HttpResponseException e) {
        log.error(
                "[Polish API] Error handler - HttpResponseException occurred - attempting to handle...");
        tryHandleOAuth2ErrorResponse(e);
        tryHandleErrorResponse(e);

        log.error("[Polish API] Error handler - Unhandled issue - please add handling!");
        throw e;
    }

    private static void tryHandleOAuth2ErrorResponse(HttpResponseException e) {
        ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
        String error = errorResponse.getCode();
        if (StringUtils.isNotBlank(error)) {
            if ("invalid_grant".equals(error)) {
                throw SessionError.CONSENT_INVALID.exception();
            }
        }
    }

    private static void tryHandleErrorResponse(HttpResponseException e) {
        ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
        handleBankSideIssues(e, errorResponse);
        handleCustomerAndTokenIssues(e);
        handleScaRequiredErrorResponse(errorResponse);
        handleNotImplemented(errorResponse);
    }

    private static void handleBankSideIssues(HttpResponseException e, ErrorResponse error) {
        if (RETRYABLE_STATUSES.contains(e.getResponse().getStatus())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(error.getMessage());
        } else if (TOO_MANY_REQUESTS == e.getResponse().getStatus()
                || error.getMessage().contains(DAILY_REQUEST_LIMIT_REACHED)) {
            throw BankServiceError.ACCESS_EXCEEDED.exception(error.getMessage());
        }
    }

    private static void handleScaRequiredErrorResponse(ErrorResponse error) {
        String message = error.getMessage();
        if (message.contains(DAYS_EN_90) || message.contains(DAYS_PL_90)) {
            log.warn("[Polish API] SCA required to fetch transactions for more than 90 days.");
            throw new TransactionHistoryRequiresSCAException();
        }
    }

    private static void handleCustomerAndTokenIssues(HttpResponseException e) {
        if (HttpStatus.SC_UNAUTHORIZED == e.getResponse().getStatus()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private static void handleNotImplemented(ErrorResponse error) {
        if (error.getMessage().toLowerCase().contains(NOT_IMPLEMENTED)) {
            throw new TransactionTypeNotSupportedException();
        }
    }
}
