package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Logs.LOG_TAG;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrors.DAILY_REQUEST_LIMIT_REACHED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrors.INVALID_GRANT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrors.NOT_IMPLEMENTED;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.collect.ImmutableList;
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
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public final class PolishApiErrorHandler {
    private static final int MAX_TIME_WAIT_IN_S = 15;
    private static final int MAX_NUM_OF_ATTEMPTS = 5;

    private static final int TOO_MANY_REQUESTS_STATUS = 429;
    private static final List<Integer> RETRYABLE_STATUSES =
            ImmutableList.of(
                    HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    HttpStatus.SC_SERVICE_UNAVAILABLE,
                    HttpStatus.SC_BAD_GATEWAY);

    private static final ImmutableList<String> RETRYABLE_BANK_SIDE_ISSUES =
            ImmutableList.of(
                    "connection reset", "connect timed out", "read timed out", "failed to respond");

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
            } else if (cause instanceof HttpClientException) {
                handleHttpClientException((HttpClientException) cause);
            } else {
                throw new RuntimeException(
                        LOG_TAG + " Error handler - Something bad happened when calling bank API",
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
                    LOG_TAG + " Error handler - Such request type is not supported");
        }
    }

    private static <T> Retryer<T> prepareRetryer() {
        return RetryerBuilder.<T>newBuilder()
                .retryIfException(PolishApiErrorHandler::shouldRetryRequest)
                .withWaitStrategy(
                        WaitStrategies.exponentialWait(MAX_TIME_WAIT_IN_S, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(MAX_NUM_OF_ATTEMPTS))
                .withRetryListener(getListener())
                .build();
    }

    private static boolean shouldRetryRequest(Throwable e) {
        return (e instanceof HttpResponseException
                        && isRetryableStatusReturnedByBank((HttpResponseException) e))
                || (e instanceof HttpClientException
                        && isBankSideHttpClientException((HttpClientException) e));
    }

    private static boolean isRetryableStatusReturnedByBank(HttpResponseException e) {
        return RETRYABLE_STATUSES.contains(e.getResponse().getStatus());
    }

    private static boolean isBankSideHttpClientException(HttpClientException e) {
        return RETRYABLE_BANK_SIDE_ISSUES.stream()
                .anyMatch((failure -> containsIgnoreCase(e.getMessage(), failure)));
    }

    private static RetryListener getListener() {
        return new RetryListener() {
            @Override
            public <V> void onRetry(Attempt<V> attempt) {
                if (attempt.hasException()) {
                    log.warn(
                            "{} Error handler - Retryable exception happened, retrying...",
                            LOG_TAG);
                }
            }
        };
    }

    private static void handleHttpResponseException(HttpResponseException e) {
        log.error(
                "{} Error handler - HttpResponseException occurred - attempting to handle...",
                LOG_TAG);
        tryHandleOAuth2ErrorResponse(e);
        tryHandleErrorResponse(e);

        log.error("{} Error handler - Unhandled issue - please add handling!", LOG_TAG);
        throw e;
    }

    private static void tryHandleOAuth2ErrorResponse(HttpResponseException e) {
        ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
        String error = errorResponse.getCode();
        if (StringUtils.isNotBlank(error) && INVALID_GRANT.equals(error)) {
            throw SessionError.CONSENT_INVALID.exception();
        }
    }

    private static void tryHandleErrorResponse(HttpResponseException e) {
        ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
        handleBankSideIssues(e, errorResponse);
        handleCustomerAndTokenIssues(e);
        handleTooBigTransactionsWindow(errorResponse);
        handleNotImplemented(errorResponse);
    }

    private static void handleBankSideIssues(HttpResponseException e, ErrorResponse error) {
        if (isRetryableStatusReturnedByBank(e)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(error.getMessage());
        } else if (TOO_MANY_REQUESTS_STATUS == e.getResponse().getStatus()
                || error.getMessage().contains(DAILY_REQUEST_LIMIT_REACHED)) {
            throw BankServiceError.ACCESS_EXCEEDED.exception(error.getMessage());
        }
    }

    private static void handleTooBigTransactionsWindow(ErrorResponse error) {
        String message = error.getMessage();
        if (PolishApiErrors.isTooBigTransactionHistoryWindow(message)) {
            log.warn("{} SCA required to fetch transactions for more than 90 days.", LOG_TAG);
            throw new TransactionHistoryRequiresSCAException();
        }
    }

    private static void handleCustomerAndTokenIssues(HttpResponseException e) {
        HttpResponse response = e.getResponse();
        if (HttpStatus.SC_UNAUTHORIZED == response.getStatus()
                && !PolishApiErrors.isTooBigTransactionHistoryWindow(
                        response.getBody(String.class))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private static void handleNotImplemented(ErrorResponse error) {
        if (error.getMessage().toLowerCase().contains(NOT_IMPLEMENTED)) {
            throw new TransactionTypeNotSupportedException();
        }
    }

    private static void handleHttpClientException(HttpClientException e) {
        log.error("{} Error handler - HttpClientException occurred...", LOG_TAG);
        if (isBankSideHttpClientException(e)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
        }
    }
}
