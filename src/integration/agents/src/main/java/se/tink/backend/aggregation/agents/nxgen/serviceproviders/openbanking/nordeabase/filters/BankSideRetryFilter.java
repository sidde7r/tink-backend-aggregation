package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.filters;

import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BankSideRetryFilter extends AbstractRandomRetryFilter {

    public BankSideRetryFilter() {
        super(3, 1000);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return BankSideFailureFilter.isBankSideFailure(response);
    }
}
