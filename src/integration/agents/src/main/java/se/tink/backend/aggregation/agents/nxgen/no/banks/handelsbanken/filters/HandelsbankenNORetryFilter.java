package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.filters;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class HandelsbankenNORetryFilter extends AbstractRandomRetryFilter {

    private static final List<String> RETRY_ERROR_BODIES =
            ImmutableList.of("unhandled exception has occured");

    public HandelsbankenNORetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == 500
                && RETRY_ERROR_BODIES.stream().anyMatch(response.getBody(String.class)::contains);
    }
}
