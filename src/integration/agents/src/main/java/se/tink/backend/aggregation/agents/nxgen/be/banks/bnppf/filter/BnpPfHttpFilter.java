package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.filter;

import java.util.Objects;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class BnpPfHttpFilter extends Filter {

    private static final String CSRF_HEADER_KEY = "CSRF";
    private static final String CSRF_HEADER_VALUE = "abcdefghijk";

    private String accessToken;

    public BnpPfHttpFilter(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        if (Objects.equals(HttpMethod.POST, httpRequest.getMethod())) {
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }

        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.add(CSRF_HEADER_KEY, CSRF_HEADER_VALUE);

        HttpResponse httpResponse = nextFilter(httpRequest);

        return httpResponse;
    }
}
