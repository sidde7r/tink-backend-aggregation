package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class CbiGlobeBperRetryFilter extends AbstractRandomRetryFilter {

    private static final String AUTHENTICATION_REQUIRED = "authentication required";

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     */
    public CbiGlobeBperRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return authenticationRequired(response);
    }

    // BPER Banca has issues with the timing of the status check vs the time it takes to
    // authenticate. It needs a longer sleep than the main CBIGlobe filter (ITE-2191)
    private boolean authenticationRequired(HttpResponse response) {
        return response.getStatus() == 400
                && Optional.ofNullable(response.getBody(String.class))
                        .map(String::toLowerCase)
                        .filter(x -> x.contains(AUTHENTICATION_REQUIRED))
                        .isPresent();
    }
}
