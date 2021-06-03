package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.filters;

import com.google.api.client.http.HttpStatusCodes;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SocieteGeneraleRetryFilter extends AbstractRandomRetryFilter {

    public SocieteGeneraleRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return (response.getStatus() == HttpStatusCodes.STATUS_CODE_SERVICE_UNAVAILABLE);
    }
}
