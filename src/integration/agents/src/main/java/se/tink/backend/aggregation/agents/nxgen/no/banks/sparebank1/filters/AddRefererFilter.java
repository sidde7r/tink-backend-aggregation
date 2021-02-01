package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.filters;

import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Headers;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class AddRefererFilter extends Filter {
    private String previousRequestUri;
    private String lastUsedRefererUri;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        if (!Strings.isNullOrEmpty(previousRequestUri)) {
            final String nextReferer;

            // Don't update referer header if we keep sending requests to the same uri (polling)
            if (Objects.equals(httpRequest.getURI().toASCIIString(), previousRequestUri)) {
                nextReferer = lastUsedRefererUri;
            } else {
                nextReferer = previousRequestUri;
            }

            httpRequest.getHeaders().add(Headers.REFERER, nextReferer);
        }

        previousRequestUri = httpRequest.getURI().toASCIIString();

        HttpResponse response = nextFilter(httpRequest);

        // If response status is 3xx we keep using the current referer
        if (response.getStatus() < 300 && response.getStatus() >= 400) {
            lastUsedRefererUri = previousRequestUri;
            previousRequestUri = httpRequest.getURI().toASCIIString();
        }

        return response;
    }
}
