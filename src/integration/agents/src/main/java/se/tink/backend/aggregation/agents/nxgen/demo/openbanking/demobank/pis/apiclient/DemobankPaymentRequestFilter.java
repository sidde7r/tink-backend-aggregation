package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage.DemobankStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class DemobankPaymentRequestFilter extends Filter {

    private final DemobankStorage storage;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        addAuthorizationHeader(httpRequest.getHeaders());

        return nextFilter(httpRequest);
    }

    private void addAuthorizationHeader(MultivaluedMap<String, Object> headers) {
        final OAuth2Token accessToken =
                storage.getAccessToken().orElseThrow(IllegalArgumentException::new);

        headers.add(HttpHeaders.AUTHORIZATION, accessToken.toAuthorizeHeader());
    }
}
