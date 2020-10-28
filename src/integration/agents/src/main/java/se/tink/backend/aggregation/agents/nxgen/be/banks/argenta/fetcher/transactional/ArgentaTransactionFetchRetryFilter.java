package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional;

import java.util.LinkedList;
import java.util.List;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ArgentaTransactionFetchRetryFilter extends AbstractRetryFilter {

    List<URL> repeated = new LinkedList<>();

    public ArgentaTransactionFetchRetryFilter(long retrySleepMilliseconds) {
        super(1, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        if (isUnauthorizedResponseDueToTooFastTransactionPaging(response)
                && !repeated.contains(response.getRequest().getUrl())) {
            repeated.add(response.getRequest().getUrl());
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }

    private boolean isUnauthorizedResponseDueToTooFastTransactionPaging(HttpResponse response) {
        return isFetchTransactionRequest(response)
                && response.getStatus() == 401
                && response.getBody(String.class).contains("error.unauthorized");
    }

    private boolean isFetchTransactionRequest(HttpResponse response) {
        return response.getRequest().getUrl().toString().contains("transactions?page");
    }
}
