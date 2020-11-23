package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.filters;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class DnbRetryFilter extends AbstractRandomRetryFilter {

    private static final List<String> RETRY_ERROR_BODIES =
            ImmutableList.of(
                    DnbConstants.Messages.SERVER_UNAVAILABLE,
                    DnbConstants.Messages.SERVICE_NOT_AVAILABLE_PREFIX,
                    DnbConstants.Messages.TRY_IN_A_FEW_MINUTES_PREFIX,
                    DnbConstants.Messages.TRY_IN_5_MINUTES_PREFIX);

    private static final List<String> RETRY_ERROR_BODIES_EXCEPTIONS =
            ImmutableList.of(DnbConstants.Messages.NO_ACCOUNT_SUFFIX);

    public DnbRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        if (response.hasBody()) {
            String body = response.getBody(String.class);
            return RETRY_ERROR_BODIES.stream().anyMatch(body::contains)
                    && RETRY_ERROR_BODIES_EXCEPTIONS.stream().noneMatch(body::contains);
        }
        return false;
    }
}
