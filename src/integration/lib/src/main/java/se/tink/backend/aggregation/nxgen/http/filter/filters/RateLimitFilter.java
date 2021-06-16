package se.tink.backend.aggregation.nxgen.http.filter.filters;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.rate_limit_service.RateLimitService;

@Slf4j
/**
 * Rate Limit Filter: catches HTTP 429 responses, notifies rate limit service and optionally retries
 * the request after a random time.
 */
public class RateLimitFilter extends Filter {
    private final String providerName;
    private long retrySleepMillisecondsMin = 0;
    private long retrySleepMillisecondsMax = 0;
    private long maxRetries = 0;

    public RateLimitFilter(String providerName) {
        this.providerName =
                Preconditions.checkNotNull(
                        Strings.emptyToNull(providerName), "Provider name must not be null");
    }

    public RateLimitFilter(
            String providerName, long retrySleepMillisecondsMin, long retrySleepMillisecondsMax) {
        this(providerName, retrySleepMillisecondsMin, retrySleepMillisecondsMax, 1);
    }

    public RateLimitFilter(
            String providerName,
            long retrySleepMillisecondsMin,
            long retrySleepMillisecondsMax,
            long maxRetries) {
        this(providerName);
        Preconditions.checkArgument(
                retrySleepMillisecondsMax >= retrySleepMillisecondsMin,
                "Maximum retry time must not be lower than minimum retry time");
        Preconditions.checkArgument(
                retrySleepMillisecondsMin > 0, "Retry time must be greater than zero");
        Preconditions.checkArgument(maxRetries > 0, "Max retries should be more than zero");
        this.retrySleepMillisecondsMin = retrySleepMillisecondsMin;
        this.retrySleepMillisecondsMax = retrySleepMillisecondsMax;
        this.maxRetries = maxRetries;
    }

    @Override
    public HttpResponse handle(final HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (isRateLimitResponse(response)) {
            final String body = response.getBody(String.class);
            final BankServiceException ex =
                    BankServiceError.ACCESS_EXCEEDED.exception(
                            "Http status: " + response.getStatus() + " Error body: " + body);
            RateLimitService.INSTANCE.notifyRateLimitExceeded(providerName, ex);

            for (long retry = 1; retry <= maxRetries; retry++) {
                long retrySleepMilliseconds = calculateRetryTime();
                log.warn(
                        "[RateLimitFilter] Got rate limited, retrying in {}ms ({}/{})",
                        retrySleepMilliseconds,
                        retry,
                        maxRetries);
                Uninterruptibles.sleepUninterruptibly(
                        retrySleepMilliseconds, TimeUnit.MILLISECONDS);
                response = nextFilter(httpRequest);
                if (!isRateLimitResponse(response)) {
                    log.warn("[RateLimitFilter] Success after retrying {} time(s)", retry);
                    return response;
                }
            }

            log.warn("[RateLimitFilter] Got rate limited after {} retries, giving up.", maxRetries);
            throw ex;
        }

        return response;
    }

    protected boolean isRateLimitResponse(HttpResponse response) {
        // HTTP 429: Too Many Requests
        return response.getStatus() == 429;
    }

    private long calculateRetryTime() {
        if (retrySleepMillisecondsMin == retrySleepMillisecondsMax) {
            // constant or no retry
            return retrySleepMillisecondsMin;
        }

        final long retryRange = retrySleepMillisecondsMax - retrySleepMillisecondsMin;
        return retrySleepMillisecondsMin + (long) (retryRange * new Random().nextDouble());
    }
}
