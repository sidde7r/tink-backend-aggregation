package se.tink.backend.aggregation.nxgen.http.filter.filters.retry;

import javax.net.ssl.SSLHandshakeException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SslHandshakeRetryFilter extends AbstractRetryFilter {

    public SslHandshakeRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return false;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return exception instanceof HttpClientException
                && exception.getCause() instanceof SSLHandshakeException;
    }
}
