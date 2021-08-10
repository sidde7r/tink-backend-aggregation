package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.Log.BEC_LOG_TAG;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter.BecBankUnavailableUtil.isBankUnavailableErrorMessage;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter.BecBankUnavailableUtil.isBankUnavailableStatus;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class BecBankUnavailableRetryFilter extends AbstractRetryFilter {

    public BecBankUnavailableRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        boolean shouldRetry =
                isBankUnavailableStatus(response.getStatus())
                        && response.hasBody()
                        && isBankUnavailableErrorMessage(response.getBody(String.class));
        if (shouldRetry) {
            log.info("{} Retrying request on bank function not being available.", BEC_LOG_TAG);
        }
        return shouldRetry;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
