package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.TokenErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class VolksbankRetryFilter extends AbstractRandomRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     */
    public VolksbankRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                && !isInvalidRequestError(response);
    }

    private boolean isInvalidRequestError(HttpResponse response) {
        TokenErrorResponse errorResponse = response.getBody(TokenErrorResponse.class);

        return errorResponse != null && errorResponse.isInvalidRequest();
    }
}
