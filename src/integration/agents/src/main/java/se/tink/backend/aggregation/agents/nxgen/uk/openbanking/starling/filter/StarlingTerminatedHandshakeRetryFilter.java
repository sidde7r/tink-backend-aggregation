package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.filter;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class StarlingTerminatedHandshakeRetryFilter extends Filter {

    private static final String REMOTE_HOST_TERMINATED = "Remote host terminated the handshake";
    private final int maxRetries;
    private final int retrySleepMilliseconds;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        int retryCount = 0;
        while (anyRetriesLeft(retryCount)) {
            try {
                return nextFilter(httpRequest);
            } catch (RuntimeException e) {
                retryCount += 1;
                if (shouldRetryAttempt(e, retryCount)) {
                    log.warn(
                            "[StarlingTerminatedHandshakeRetryFilter] Filter caught retryable exception, retrying [{}/{}]",
                            retryCount,
                            maxRetries,
                            e);
                    Uninterruptibles.sleepUninterruptibly(
                            retrySleepMilliseconds, TimeUnit.MILLISECONDS);
                    continue;
                }
                if (hasRemoteHostTerminatedException(e)) {
                    throw BankServiceError.BANK_SIDE_FAILURE.exception();
                }
                throw e;
            }
        }
        throw new IllegalStateException(
                "[StarlingTerminatedHandshakeRetryFilter] This code should not be reached. TerminatedHandshakeFilter::handle should only result in a successful response or HttpClientException.");
    }

    private boolean shouldRetryAttempt(RuntimeException e, int retryCount) {
        return hasRemoteHostTerminatedException(e) && anyRetriesLeft(retryCount);
    }

    public boolean hasRemoteHostTerminatedException(Exception exception) {
        return exception.getMessage().equalsIgnoreCase(REMOTE_HOST_TERMINATED);
    }

    private boolean anyRetriesLeft(int retryCount) {
        return retryCount <= maxRetries;
    }
}
