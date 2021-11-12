package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.filter;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

// Currently Barclays is returning a lot of 502 for our BG refreshes, we are using
// BarclaysRetryFilter to overcome this problem.
// Ticket on UKOB service desk with detail: OBSD-26678
public class BarclaysRetryFilter extends AbstractRetryFilter {

    public BarclaysRetryFilter(int maxRetries, long retrySleepMilliseconds) {
        super(maxRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        int statusCode = response.getStatus();
        return statusCode == 502;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        if (exception instanceof BankServiceException) {
            return true;
        }
        if (exception instanceof HttpResponseException) {
            HttpResponseException responseException = (HttpResponseException) exception;
            return responseException.getResponse().getStatus() == 502;
        }
        return false;
    }
}
