package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLHandshakeException;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/**
 * Retry in any case that a potential temporary error is encounter. The conditions in that class are
 * created based on refreshes that ended up in TEMPORARY_ERROR.
 */
@Slf4j
public class SibsRetryFilter extends AbstractRetryFilter {

    private static final List<Integer> RETRY_ON_HTTP_CODES = Arrays.asList(400, 500, 502, 503);

    public SibsRetryFilter(int maxNumRetries, int retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        if (RETRY_ON_HTTP_CODES.contains(response.getStatus())) {
            log.info(
                    "Retrying on a response with status: {} and body: {}",
                    response.getStatus(),
                    response.getBody(String.class));
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        if (exception instanceof HttpClientException
                && exception.getCause() instanceof SSLHandshakeException) {
            log.info(
                    "Retrying on exception: {}, with cause: {}, and message: {}",
                    exception.getClass().getSimpleName(),
                    exception.getCause().toString(),
                    exception.getMessage());
            return true;
        }
        return false;
    }
}
