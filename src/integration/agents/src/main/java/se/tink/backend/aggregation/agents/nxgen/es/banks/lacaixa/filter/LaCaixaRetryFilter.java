package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class LaCaixaRetryFilter extends AbstractRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     */
    public LaCaixaRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_CONFLICT) {
            LaCaixaErrorResponse errorResponse = response.getBody(LaCaixaErrorResponse.class);
            return errorResponse.isTemporaryProblem();
        }
        return false;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
