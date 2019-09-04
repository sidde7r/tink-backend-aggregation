package se.tink.backend.aggregation.agents.utils.jersey;

import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Copied from se.tink.backend.aggregation.nxgen.http.filter.AbstractRetryFilter with minor
// modification (because legacy agent don't use
// se.tink.backend.aggregation.nxgen.http.TinkHttpClient)
public class JerseyTimeoutRetryFilter extends ClientFilter {
    private static final Logger log = LoggerFactory.getLogger(JerseyTimeoutRetryFilter.class);
    private final int maxNumRetries;
    private final long retrySleepMilliseconds;

    public JerseyTimeoutRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        this.maxNumRetries = maxNumRetries;
        this.retrySleepMilliseconds = retrySleepMilliseconds;
    }

    @Override
    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
        for (int retryCount = 0; retryCount <= maxNumRetries; retryCount++) {

            try {
                ClientResponse httpResponse = getNext().handle(clientRequest);
                return httpResponse;
            } catch (RuntimeException e) {

                if (shouldRetry(e) && !isLastAttempt(retryCount)) {
                    log.warn(
                            "Filter caught retryable exception, retrying [{}/{}]",
                            retryCount + 1,
                            maxNumRetries,
                            e);

                    Uninterruptibles.sleepUninterruptibly(
                            retrySleepMilliseconds, TimeUnit.MILLISECONDS);

                    continue;
                }

                throw e;
            }
        }

        // Since the for loop is there to guard against an infinite loop we can instead end up
        // in this state if the terminating condition is incorrectly specified.
        throw new IllegalStateException(
                "This code should not be reached. AbstractRetryFilter::handle should only result in a successful response or HttpClientException.");
    }

    private boolean shouldRetry(RuntimeException exception) {
        return ExceptionUtils.indexOfType(exception, SocketTimeoutException.class) >= 0;
    }

    private boolean isLastAttempt(int retryCount) {
        return retryCount >= maxNumRetries;
    }
}
