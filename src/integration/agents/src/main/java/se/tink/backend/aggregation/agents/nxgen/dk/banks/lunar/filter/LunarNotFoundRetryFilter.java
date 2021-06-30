package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.filter;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.LogTags.LUNAR_TAG;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class LunarNotFoundRetryFilter extends AbstractRandomRetryFilter {

    public LunarNotFoundRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        if (response.getStatus() == 404) {
            log.info("{} Retrying on 404 response", LUNAR_TAG);
            return true;
        }
        return false;
    }
}
