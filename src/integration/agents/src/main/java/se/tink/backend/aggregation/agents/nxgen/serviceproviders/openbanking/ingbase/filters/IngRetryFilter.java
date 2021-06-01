package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class IngRetryFilter extends AbstractRandomRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     */
    public IngRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return isSignatureVerifyFailed(response)
                || ErrorMessages.ERROR_CODES.contains(response.getStatus());
    }

    private boolean isSignatureVerifyFailed(HttpResponse response) {
        return response.getStatus() == 401
                && response.getBody(String.class)
                        .contains("Signature could not be successfully verified");
    }
}
