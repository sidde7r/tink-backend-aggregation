package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class CbiGlobeUnknownResourceRetryFilter extends AbstractRetryFilter {

    public CbiGlobeUnknownResourceRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        boolean shouldRetry = false;
        if (response.getStatus() == HttpStatus.SC_FORBIDDEN) {
            ErrorResponse errorResponse = ErrorResponse.createFrom(response);
            if (errorResponse != null
                    && errorResponse.tppMessagesContainsError(
                            "RESOURCE_UNKNOWN",
                            "The addressed resource is unknown relative to the TPP.")) {
                log.info("Received HTTP 403 response. Retrying...");
                shouldRetry = true;
            }
        }
        return shouldRetry;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
