package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.Log.BEC_LOG_TAG;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class BecBankUnavailableRetryFilter extends AbstractRetryFilter {

    private static final String DK_BANK_UNAVAILABLE =
            "Den ønskede funktion er ikke tilgængelig i øjeblikket.";
    private static final String EN_BANK_UNAVAILABLE =
            "The required function is not currently available. Try again later.";

    public BecBankUnavailableRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == 400
                && response.hasBody()
                && isBankUnavailableErrorMessage(response.getBody(String.class));
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }

    private boolean isBankUnavailableErrorMessage(String response) {
        if (response.contains(DK_BANK_UNAVAILABLE) || response.contains(EN_BANK_UNAVAILABLE)) {
            log.info("{} Retrying request on bank function not being available.", BEC_LOG_TAG);
            return true;
        }
        return false;
    }
}
