package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.filters;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class DnbRetryFilter extends AbstractRandomRetryFilter {

    private static final List<String> RETRY_ERROR_BODIES =
            ImmutableList.of(
                    "The server is temporarily unable to service your request",
                    "Vi beklager at tjenesten",
                    "fem minutter",
                    "Vent 5 minutter");

    public DnbRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        String body = response.getBody(String.class);
        return RETRY_ERROR_BODIES.stream().anyMatch(body::contains);
    }
}
