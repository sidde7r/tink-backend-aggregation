package se.tink.backend.aggregation.agents.nxgen.de.openbanking.comdirect.filter;

import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ServerErrorRetryFilter;

public class ComdirectRetryFilter extends ServerErrorRetryFilter {

    public ComdirectRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }
}
