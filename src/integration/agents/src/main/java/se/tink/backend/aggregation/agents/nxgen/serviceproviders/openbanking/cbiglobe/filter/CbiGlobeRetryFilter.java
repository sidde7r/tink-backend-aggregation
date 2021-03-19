package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter;

import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class CbiGlobeRetryFilter extends AbstractRandomRetryFilter {

    private static final String CONSENT_IN_USE = "consent_already_in_use";

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     */
    public CbiGlobeRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_BAD_GATEWAY
                || response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                || response.getStatus() == HttpStatus.SC_GATEWAY_TIMEOUT
                || response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE
                || isConsentAlreadyInUseError(response);
    }

    private boolean isConsentAlreadyInUseError(HttpResponse response) {
        return response.getStatus() == 429
                && Optional.ofNullable(response.getBody(String.class))
                        .map(String::toLowerCase)
                        .filter(x -> x.contains(CONSENT_IN_USE))
                        .isPresent();
    }
}
