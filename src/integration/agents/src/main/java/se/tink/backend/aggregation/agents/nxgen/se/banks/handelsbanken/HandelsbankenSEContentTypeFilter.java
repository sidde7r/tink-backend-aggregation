package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

/**
 * Handelsbanken SE intermittently "forget" to set their <code>Content-Type</code> header for the
 * keep alive (XML) response, which breaks our HTTP body parsing since no handler is registered for
 * the default fallback <code>application/octet-stream</code>. To mitigate this, we treat responses
 * without the <code>Content-Type</code> header as <code>text/xml</code> for Handelsbanken SE.
 */
public class HandelsbankenSEContentTypeFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        HttpResponse response = nextFilter(httpRequest);

        if (!response.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE)) {
            response.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML);
        }

        return response;
    }
}
