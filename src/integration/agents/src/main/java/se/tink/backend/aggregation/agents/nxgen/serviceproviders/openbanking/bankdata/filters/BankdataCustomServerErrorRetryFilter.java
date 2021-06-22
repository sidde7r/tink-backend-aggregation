package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.filters;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class BankdataCustomServerErrorRetryFilter extends AbstractRetryFilter {

    public BankdataCustomServerErrorRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        if (BankdataCustomServerErrorFilter.isServerErrorResponse(response)) {
            log.info("[Bankdata] Retrying on server error response");
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        // do not retry on low level client exceptions
        return false;
    }
}
