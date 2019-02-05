package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.filters;

import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Objects;
import java.util.Optional;

public class KbcHttpFilter extends Filter {
    private String xXsrfToken;

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        headers.add(KbcConstants.Headers.COMPANY_KEY, KbcConstants.Headers.COMPANY_VALUE);
        headers.add(KbcConstants.Headers.APPVERSION_KEY, KbcConstants.Headers.APPVERSION_VALUE);

        if (xXsrfToken != null) {
            headers.add(KbcConstants.Headers.X_XSRF_TOKEN, xXsrfToken);
        }

        if (Objects.equals(HttpMethod.POST, httpRequest.getMethod())) {
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }

        HttpResponse httpResponse = nextFilter(httpRequest);

        Optional<String> newXsrfToken = httpResponse.getCookies().stream()
                .filter(cookie -> KbcConstants.Predicates.XSRF_TOKEN.equalsIgnoreCase(cookie.getName()))
                .map(cookie -> cookie.getValue())
                .findFirst();

        if (newXsrfToken.isPresent()) {
            xXsrfToken = newXsrfToken.get();
        }

        return httpResponse;
    }

    public void resetHttpFilter() {
        xXsrfToken = null;
    }
}
