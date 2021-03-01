package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.filter;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class UKOpenBankingPisRequestRetryFilter extends AbstractRetryFilter {
    private static final List<Integer> RETRYABLE_STATUSES =
            Arrays.asList(
                    HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    HttpStatus.SC_SERVICE_UNAVAILABLE,
                    HttpStatus.SC_BAD_GATEWAY);

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     */
    public UKOpenBankingPisRequestRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        boolean retry = RETRYABLE_STATUSES.contains(response.getStatus());
        if (retry) {
            log.info("[UKOB] Received HTTP {} response. Retrying...", response.getStatus());
        } else if (response.getStatus() >= HttpStatus.SC_BAD_REQUEST) {
            log.warn(
                    "[UKOB] Received HttpStatus:{}, response body:{}",
                    response.getStatus(),
                    response.getBody(String.class));
        }
        return retry;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
