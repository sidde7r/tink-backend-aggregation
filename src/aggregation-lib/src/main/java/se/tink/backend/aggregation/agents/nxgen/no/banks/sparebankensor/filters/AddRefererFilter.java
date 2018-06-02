package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.filters;

import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class AddRefererFilter extends Filter {
    private String previousRequestUri;
    private String lastUsedRefererUri;

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {

        if (!Strings.isNullOrEmpty(previousRequestUri)) {
            final String nextReferer;

            // Don't update referer header if we keep sending requests to the same uri (polling)
            if (Objects.equals(httpRequest.getURI().toASCIIString(), previousRequestUri)) {
                nextReferer = lastUsedRefererUri;
            } else {
                nextReferer = previousRequestUri;
            }

            httpRequest.getHeaders().add("Referer", nextReferer);
        }

        lastUsedRefererUri = previousRequestUri;
        previousRequestUri = httpRequest.getURI().toASCIIString();

        return nextFilter(httpRequest);
    }
}
