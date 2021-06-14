package se.tink.backend.aggregation.nxgen.http.filter;

import java.util.List;
import org.assertj.core.util.Arrays;
import org.assertj.core.util.Preconditions;
import org.junit.Ignore;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Ignore
public class MockResponseFilter extends Filter {
    private final List<Object> responsesOrExceptions;

    public MockResponseFilter(Object... responsesOrExceptions) {
        this.responsesOrExceptions = Arrays.asList(responsesOrExceptions);
        Preconditions.checkArgument(
                this.responsesOrExceptions.stream()
                        .allMatch(MockResponseFilter::isResponseOrException),
                "All arguments must be HttpResponse, HttpClientException or HttpResponseException");
    }

    private static boolean isResponseOrException(Object obj) {
        return obj instanceof HttpResponse
                || obj instanceof HttpClientException
                || obj instanceof HttpResponseException;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        Preconditions.checkState(!responsesOrExceptions.isEmpty(), "Out of responses");
        final Object response = responsesOrExceptions.remove(0);
        if (response instanceof HttpResponse) {
            return (HttpResponse) response;
        } else {
            throw (RuntimeException) response;
        }
    }
}
