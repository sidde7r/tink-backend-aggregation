package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bper.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BperPaymentRetryFilter extends AbstractRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     */
    public BperPaymentRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    // This is only public because tests live in ..cbi.. subpackages for clarity :/
    public boolean shouldRetry(HttpResponse response) {
        boolean shouldRetry = false;
        if (response.getStatus() == HttpStatus.SC_FORBIDDEN) {
            ErrorResponse errorResponse = ErrorResponse.createFrom(response);
            if (errorResponse != null
                    && errorResponse.tppMessagesContainsError(
                            "GENERIC_ERROR", "Unknown Payment Identifier")) {
                log.info("Received HTTP 403 Unknown Payment Identifier response. Retrying...");
                shouldRetry = true;
            }
        }
        if (response.getStatus() == HttpStatus.SC_BAD_REQUEST) {
            ErrorResponse errorResponse = ErrorResponse.createFrom(response);
            if (errorResponse != null
                    && errorResponse.errorManagementDescriptionEquals(
                            "Operation not allowed: authentication required.")) {
                log.info("Received HTTP 400 authentication required response. Retrying...");
                shouldRetry = true;
            }
        }
        if (response.getStatus() == HttpStatus.SC_BAD_GATEWAY) {
            ErrorResponse errorResponse = ErrorResponse.createFrom(response);
            if (errorResponse != null
                    && errorResponse.errorManagementDescriptionEquals(
                            "Invalid response received by the ASPSP system")) {
                log.info("Received HTTP 502 invalid response. Retrying...");
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
