package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.filter;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.ErrorMessages;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class CitadeleRetryFilter extends AbstractRandomRetryFilter {

    public CitadeleRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return ErrorMessages.ERROR_RESPONSES.contains(response.getStatus());
    }
}
