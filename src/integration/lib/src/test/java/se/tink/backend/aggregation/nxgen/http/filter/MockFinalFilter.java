package se.tink.backend.aggregation.nxgen.http.filter;

import java.net.SocketTimeoutException;

import org.junit.Ignore;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Ignore
public class MockFinalFilter extends Filter {

    private final int throwExceptionUntilAttempt;

    private int callCount;

    public MockFinalFilter(int throwExceptionUntilAttempt) {
        this.throwExceptionUntilAttempt = throwExceptionUntilAttempt;
        this.callCount = 0;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        callCount++;

        if (callCount < throwExceptionUntilAttempt) {
            throw new HttpClientException(
                    "Fake Time Out",
                    new SocketTimeoutException("Fake Socket Time Out"),
                    false,
                    false,
                    null);
        }

        return null;
    }

    public int getCallCount() {
        return callCount;
    }
}
