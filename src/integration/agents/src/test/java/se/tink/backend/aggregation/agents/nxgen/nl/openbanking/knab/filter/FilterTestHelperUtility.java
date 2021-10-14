package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Ignore
public final class FilterTestHelperUtility {

    public static HttpResponse mockResponse(int statusCode, String responseBody) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(statusCode);
        return mocked;
    }
}
