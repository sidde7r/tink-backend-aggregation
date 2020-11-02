package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.filter;

import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.Headers;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaSEFilter extends Filter {
    private int requestId = 1;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        httpRequest.getHeaders().add(Headers.REQUEST_ID, requestId++);

        return nextFilter(httpRequest);
    }
}
