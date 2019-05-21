package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.filters;

import com.google.common.util.concurrent.Uninterruptibles;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

/**
 * Will retry sending a request the number of specified times if it fails because of a read timeout.
 * This typically happens because of throttling or bank side server issues.
 */
public class SwedbankTimeoutRetryFilter extends Filter {
    private static final AggregationLogger log =
            new AggregationLogger(SwedbankTimeoutRetryFilter.class);

    private final int numRetries;
    private final long retrySleepMilliseconds;

    public SwedbankTimeoutRetryFilter(int numRetries, long retrySleepMilliseconds) {
        this.numRetries = numRetries;
        this.retrySleepMilliseconds = retrySleepMilliseconds;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        int numRetriesLeft = numRetries;

        while (true) {
            try {

                return nextFilter(httpRequest);
            } catch (HttpClientException e) {

                if (!isCauseTimeout(e.getCause()) || numRetriesLeft <= 0) {
                    throw e;
                }

                log.error(
                        String.format(
                                "SwedbankRetryFilter caught TimeOut exception, retrying %d more times.",
                                numRetriesLeft),
                        e);

                numRetriesLeft--;
                Uninterruptibles.sleepUninterruptibly(
                        retrySleepMilliseconds, TimeUnit.MILLISECONDS);
            }
        }
    }

    private boolean isCauseTimeout(Throwable cause) {
        return cause instanceof SocketTimeoutException;
    }
}
