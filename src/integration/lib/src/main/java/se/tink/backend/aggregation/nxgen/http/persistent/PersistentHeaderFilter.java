package se.tink.backend.aggregation.nxgen.http.persistent;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class PersistentHeaderFilter extends Filter {
    private HashSet<Header> headers = Sets.newHashSet();

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        headers.forEach(h -> httpRequest.getHeaders().add(h.getKey(), h.getValue()));
        return nextFilter(httpRequest);
    }

    public void addHeader(Header header) {
        if (headers.contains(header)) {
            headers.remove(header);
        }

        headers.add(header);
    }

    public void setHeaders(HashSet<Header> headers) {
        this.headers = headers;
    }

    public HashSet<Header> getHeaders() {
        return headers;
    }

    public void clearHeaders() {
        headers.clear();
    }

    public boolean isHeaderPresent(String key) {
        return headers != null
                && headers.stream().anyMatch(header -> Objects.equals(key, header.getKey()));
    }
}
