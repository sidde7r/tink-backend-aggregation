package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/**
 * Filter to retry on an error response when a SIBS API call responds with <code>
 * HTTP 405 Method not allowed</code>.
 *
 * <p>Typical response body:
 *
 * <p>{"transactionStatus":"RJCT","tppMessages":[{"category":"ERROR","code":"SERVICE_INVALID","text":
 * "The addressed service is not valid for the addressed resources."}]}
 */
@Slf4j
public class SibsServiceInvalidRetryFilter extends AbstractRetryFilter {

    private static final String ERROR_CODE = "SERVICE_INVALID";

    public SibsServiceInvalidRetryFilter(int maxNumRetries, int retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_METHOD_NOT_ALLOWED
                && bodyContainsServiceInvalidErrorCode(response);
    }

    private boolean bodyContainsServiceInvalidErrorCode(HttpResponse response) {
        return Optional.ofNullable(response.getBody(String.class))
                .map(body -> body.contains(ERROR_CODE))
                .orElse(false);
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
