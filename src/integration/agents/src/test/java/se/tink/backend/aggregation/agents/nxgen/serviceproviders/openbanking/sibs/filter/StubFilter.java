package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import org.junit.Ignore;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Ignore
public final class StubFilter extends Filter {
    private HttpResponse response = Mockito.mock(HttpResponse.class);

    @Override
    public HttpResponse handle(final HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }
}
