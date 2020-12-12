package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.filters;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class TransactionFetchRetryFilter extends AbstractRandomRetryFilter {

    public TransactionFetchRetryFilter() {
        super(3, 1000);
    }

    // Encountered this error with commerzbank. Their pagination system seems to lag a bit,
    // returning an error about page parameter they suggested in the first place.
    // Retrying after a moment seems to help.
    @Override
    protected boolean shouldRetry(HttpResponse response) {
        if (response.getStatus() == 503) {
            return true;
        } else if (response.getStatus() == 400 && response.hasBody()) {
            ErrorResponse errorResponse = getBodyAsExpectedType(response);
            return errorResponse != null
                    && errorResponse.getTppMessages() != null
                    && errorResponse.getTppMessages().stream()
                            .anyMatch(ErrorEntity.PARAMETER_NOT_CONSISTENT::equals);
        } else {
            return false;
        }
    }

    private ErrorResponse getBodyAsExpectedType(HttpResponse response) {
        try {
            return response.getBody(ErrorResponse.class);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
