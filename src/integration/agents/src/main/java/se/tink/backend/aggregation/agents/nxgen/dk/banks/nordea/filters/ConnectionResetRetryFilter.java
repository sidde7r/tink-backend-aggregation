package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.filters;

import javax.net.ssl.SSLException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;

public class ConnectionResetRetryFilter extends AbstractRandomRetryFilter {

    public ConnectionResetRetryFilter() {
        super(3, 1000);
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return exception instanceof HttpClientException
                && exception.getCause() instanceof SSLException;
    }
}
