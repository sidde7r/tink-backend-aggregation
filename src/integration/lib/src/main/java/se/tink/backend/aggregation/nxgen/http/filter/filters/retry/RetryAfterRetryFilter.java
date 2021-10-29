package se.tink.backend.aggregation.nxgen.http.filter.filters.retry;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class RetryAfterRetryFilter extends AbstractRetryFilter {

    private static final LogTag LOG_TAG = LogTag.from("[RetryAfterRetryFilter]");
    private static final String RETRY_AFTER_HEADER_NAME = "Retry-After";

    private long waitForSeconds;

    public RetryAfterRetryFilter(int maxNumRetries) {
        super(maxNumRetries, 0);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        if (response.getStatus() != 429) {
            return false;
        }

        Optional<Long> maybeRetryAfterSeconds = getPositiveRetryAfterSecondsHeaderValue(response);
        if (!maybeRetryAfterSeconds.isPresent()) {
            return false;
        }

        waitForSeconds = maybeRetryAfterSeconds.get();
        return true;
    }

    private Optional<Long> getPositiveRetryAfterSecondsHeaderValue(HttpResponse response) {
        return getRetryAfterSecondHeaderValues(response).stream()
                .findFirst()
                .map(this::tryParseLongValue)
                .filter(
                        retryAfterSeconds -> {
                            if (retryAfterSeconds < 0) {
                                log.warn(
                                        "{} Negative retry after seconds: {}",
                                        LOG_TAG,
                                        retryAfterSeconds);
                                return false;
                            }
                            return true;
                        });
    }

    private List<String> getRetryAfterSecondHeaderValues(HttpResponse response) {
        return Optional.ofNullable(response.getHeaders().get(RETRY_AFTER_HEADER_NAME))
                .orElse(Collections.emptyList());
    }

    private Long tryParseLongValue(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            log.error("{} Could not parse header value: {}", LOG_TAG, value);
            return null;
        }
    }

    @Override
    protected long getRetrySleepMilliseconds(int retry) {
        log.info(
                "{} Will retry after seconds: {}. Retry: [{}/{}]",
                LOG_TAG,
                waitForSeconds,
                retry,
                getMaxNumRetries());
        return waitForSeconds * 1_000;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
