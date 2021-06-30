package se.tink.backend.aggregation.nxgen.http.filter.filters;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

// Copied se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter
// but for Remote host terminated the handshake-error

public class TerminatedHandshakeRetryFilter extends Filter {

    private int maxNumRetries;
    private int timeoutSleepMilliseconds;
    private final Logger log = LoggerFactory.getLogger(TerminatedHandshakeRetryFilter.class);

    public TerminatedHandshakeRetryFilter(int maxNumRetries, int timeoutSleepMilliseconds) {
        this.maxNumRetries = maxNumRetries;
        this.timeoutSleepMilliseconds = timeoutSleepMilliseconds;
    }

    public TerminatedHandshakeRetryFilter() {
        this.maxNumRetries = 3;
        this.timeoutSleepMilliseconds = 1000;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException {
        for (int retryCount = 0; retryCount <= maxNumRetries; retryCount++) {

            try {
                return getNext().handle(httpRequest);
            } catch (RuntimeException e) {

                if (shouldRetry(e) && !isLastTime(retryCount)) {
                    log.warn(
                            "Filter caught retryable exception, retrying [{}/{}]",
                            retryCount + 1,
                            maxNumRetries,
                            e);

                    Uninterruptibles.sleepUninterruptibly(
                            timeoutSleepMilliseconds, TimeUnit.MILLISECONDS);

                    continue;
                }

                throw e;
            }
        }
        throw new IllegalStateException(
                "This code should not be reached. TerminatedHandshakeFilter::handle should only result in a successful response or HttpClientException.");
    }

    public boolean shouldRetry(Exception exception) {
        return exception.getMessage().equalsIgnoreCase("Remote host terminated the handshake");
    }

    public boolean isLastTime(int retryCount) {
        return (retryCount >= maxNumRetries);
    }
}
