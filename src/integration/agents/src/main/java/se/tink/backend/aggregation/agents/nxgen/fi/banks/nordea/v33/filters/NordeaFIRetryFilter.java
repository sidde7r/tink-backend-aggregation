package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.filters;

import java.util.Arrays;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class NordeaFIRetryFilter extends AbstractRandomRetryFilter {

    public NordeaFIRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    public boolean shouldRetry(HttpResponse response) {
        return Arrays.asList(HttpStatus.SC_SERVICE_UNAVAILABLE, HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .contains(response.getStatus());
    }
}
