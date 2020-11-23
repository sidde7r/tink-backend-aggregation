package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters;

import java.util.Objects;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SwedbankBaseHttpFilter extends Filter {
    private final String authorization;

    public SwedbankBaseHttpFilter(String authorization) {
        this.authorization = authorization;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        final MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, authorization);

        if (Objects.equals(HttpMethod.POST, httpRequest.getMethod())
                || Objects.equals(HttpMethod.PUT, httpRequest.getMethod())) {
            httpRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }

        return nextFilter(httpRequest);
    }
}
