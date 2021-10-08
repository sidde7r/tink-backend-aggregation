package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class LaBanquePostaleRetryFilter extends AbstractRetryFilter {

    public static final int RETRY_ATTEMPT_NUMBER = 3;
    public static final int RETRY_SLEEP_MILLISECONDS = 5000;
    public static final int STATUS_CODE_SERVER_ERROR = 500;
    public static final String ERROR_INFORMATION =
            "Error attempting to read the urlopen response data";

    public LaBanquePostaleRetryFilter() {
        super(RETRY_ATTEMPT_NUMBER, RETRY_SLEEP_MILLISECONDS);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        final int responseStatus = response.getStatus();
        final String responseBody = response.getBody(String.class);

        return responseStatus == STATUS_CODE_SERVER_ERROR
                && responseBody.contains(ERROR_INFORMATION);
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
