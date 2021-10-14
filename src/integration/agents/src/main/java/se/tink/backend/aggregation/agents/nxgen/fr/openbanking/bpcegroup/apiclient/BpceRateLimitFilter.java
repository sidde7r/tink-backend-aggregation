package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient;

import se.tink.backend.aggregation.nxgen.http.filter.filters.RateLimitFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BpceRateLimitFilter extends RateLimitFilter {

    public BpceRateLimitFilter(String providerName) {
        super(providerName, 10000, 15000, 3);
    }

    @Override
    protected boolean isRateLimitResponse(HttpResponse response) {
        int statusCode = response.getStatus();
        return statusCode == 429
                || (statusCode == 403
                        && response.getBody(BpceErrorResponse.class).isTooManyRequest());
    }
}
