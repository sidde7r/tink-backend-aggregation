package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BankdataRetryFilter extends AbstractRandomRetryFilter {
    public BankdataRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        final int status = response.getStatus();

        return status == HttpStatus.SC_INTERNAL_SERVER_ERROR
                || status == HttpStatus.SC_SERVICE_UNAVAILABLE;
    }
}
