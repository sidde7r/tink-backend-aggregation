package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLHandshakeException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/**
 * Retry in any case that a potencial temporary error is encounter. The conditions in that class are
 * created based on refreshes that ended up in TEMPORARY_ERROR.
 */
public class SibsRetryFilter extends AbstractRetryFilter {

    private static final List<Integer> RETRY_ON_HTTP_CODES = Arrays.asList(400, 429, 500, 502, 503);

    public SibsRetryFilter() {
        super(5, 1000);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return RETRY_ON_HTTP_CODES.contains(response.getStatus());
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return exception instanceof HttpClientException
                && exception.getCause() instanceof SSLHandshakeException;
    }
}
