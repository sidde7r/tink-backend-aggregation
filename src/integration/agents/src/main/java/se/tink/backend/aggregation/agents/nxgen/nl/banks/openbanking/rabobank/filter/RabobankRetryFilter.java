package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class RabobankRetryFilter extends AbstractRandomRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     */
    public RabobankRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return ErrorMessages.ERROR_RESPONSES.contains(response.getStatus())
                || isTokenUrlNotFoundError(response);
    }

    private boolean isTokenUrlNotFoundError(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_NOT_FOUND
                && response.getBody(String.class).contains(ErrorMessages.TOKEN_URL_NOT_FOUND);
    }
}
