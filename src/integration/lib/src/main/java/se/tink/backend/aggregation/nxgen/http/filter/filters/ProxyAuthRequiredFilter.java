package se.tink.backend.aggregation.nxgen.http.filter.filters;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/** Utility filter to log error from Bright Data (Luminati) proxy */
@Slf4j
public class ProxyAuthRequiredFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED == response.getStatus()) {
            log.error(
                    "External proxy error: {}", response.getHeaders().getFirst("X-Luminati-Error"));
        }

        return response;
    }
}
