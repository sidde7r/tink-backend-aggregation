package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.filter;

import se.tink.backend.aggregation.agents.exceptions.refresh.InvestmentAccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BbvaInvestmentAccountBlockedFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        BbvaErrorResponse errorResponse = response.getBody(BbvaErrorResponse.class);

        if (errorResponse.isContractNotOperableError()) {
            throw new InvestmentAccountRefreshException(
                    "The client has the contract marked as blocked to operate");
        }

        return response;
    }
}
