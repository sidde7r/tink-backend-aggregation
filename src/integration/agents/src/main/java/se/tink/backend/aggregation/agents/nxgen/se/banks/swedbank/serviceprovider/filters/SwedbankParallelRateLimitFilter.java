package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.TppErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SwedbankParallelRateLimitFilter extends AbstractRandomRetryFilter {
    private static final int ACCESS_EXCEEDED_ERROR_CODE = 429;

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     */
    public SwedbankParallelRateLimitFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        if (response.getStatus() == ACCESS_EXCEEDED_ERROR_CODE) {
            TppErrorResponse errorResponse = response.getBody(TppErrorResponse.class);
            return errorResponse != null
                    && errorResponse
                            .getErrorMessage()
                            .contains(ErrorMessage.REACHED_PARALLEL_REQUESTS_LIMIT);
        }

        return false;
    }
}
