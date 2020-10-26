package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.filters;

import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class DnbErrorsFilter extends Filter {

    private static final String NO_ACCESS_ELEMENT = "<div id=\"websealError\">no_access</div>";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (!response.hasBody()) {
            return response;
        }

        handleNoAccessError(response);
        return response;
    }

    private void handleNoAccessError(HttpResponse response) {
        if (response.getStatus() == 403
                && response.getBody(String.class).contains(NO_ACCESS_ELEMENT)) {
            throw LoginError.NOT_CUSTOMER.exception();
        }
    }
}
