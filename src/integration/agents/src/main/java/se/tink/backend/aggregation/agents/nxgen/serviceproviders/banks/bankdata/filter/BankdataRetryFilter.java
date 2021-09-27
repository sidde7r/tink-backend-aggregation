package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BankdataRetryFilter extends AbstractRandomRetryFilter {

    private static final String USER_HAS_NOT_COMPLETED_NEM_ID_LOGIN_PROCESS =
            "user has not completed nemid login process";

    public BankdataRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_BAD_REQUEST
                && response.getBody(String.class)
                        .toLowerCase()
                        .contains(USER_HAS_NOT_COMPLETED_NEM_ID_LOGIN_PROCESS);
    }
}
