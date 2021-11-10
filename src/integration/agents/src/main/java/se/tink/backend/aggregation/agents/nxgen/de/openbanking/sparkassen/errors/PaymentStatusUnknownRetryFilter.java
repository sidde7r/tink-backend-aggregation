package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors;

import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class PaymentStatusUnknownRetryFilter extends AbstractRetryFilter {

    public PaymentStatusUnknownRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return ErrorResponse.fromHttpResponse(response)
                .filter(
                        ErrorResponse.anyTppMessageMatchesPredicate(
                                SparkassenKnownErrors.PAYMENT_STATUS_UNKNOWN))
                .isPresent();
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
