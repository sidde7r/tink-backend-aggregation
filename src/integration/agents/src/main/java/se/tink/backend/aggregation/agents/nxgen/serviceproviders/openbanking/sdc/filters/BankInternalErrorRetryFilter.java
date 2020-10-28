package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.filters;

import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BankInternalErrorRetryFilter extends AbstractRandomRetryFilter {

    public BankInternalErrorRetryFilter() {
        super(2, 300);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return BankInternalErrorFilter.isBankSideFailure(response);
    }
}
