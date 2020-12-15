package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
class PermanentRedirectFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        HttpResponse httpResponse = nextFilter(httpRequest);

        if (httpResponse.getStatus() == 308) {
            String loc = httpResponse.getHeaders().getFirst("Location");
            log.info(
                    "Resources moved permanently (http status code: {}): from {} to {}",
                    httpResponse.getStatus(),
                    httpRequest.getURI(),
                    loc);
            if (loc != null) {
                httpRequest.setUrl(new URL(loc));
                httpResponse = nextFilter(httpRequest);
            }
        }
        return httpResponse;
    }
}
