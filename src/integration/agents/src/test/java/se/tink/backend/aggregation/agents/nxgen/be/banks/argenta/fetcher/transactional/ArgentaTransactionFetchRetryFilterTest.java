package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ArgentaTransactionFetchRetryFilterTest {

    private ArgentaTransactionFetchRetryFilter objectUnderTest;
    private HttpResponse httpResponse;
    private HttpRequest httpRequest;

    @Before
    public void init() {
        objectUnderTest = new ArgentaTransactionFetchRetryFilter(0);
        httpRequest = Mockito.mock(HttpRequest.class);
        httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpRequest.getUrl())
                .thenReturn(
                        new URL(
                                "https://mobile-api.argenta.be/accounts/BE26973370558929/transactions?page=100"));
        Mockito.when(httpResponse.getRequest()).thenReturn(httpRequest);
    }

    @Test
    public void shouldRepeatOnlyOnceFetchTransactionRequest() {
        // given
        mockUnauthorizedResponse();
        // when
        boolean result1 = objectUnderTest.shouldRetry(httpResponse);
        boolean result2 = objectUnderTest.shouldRetry(httpResponse);
        // then
        Assertions.assertThat(result1).isTrue();
        Assertions.assertThat(result2).isFalse();
    }

    @Test
    public void shouldNotRepeatWhenRequestIsNotForTransactionFetching() {
        // given
        Mockito.when(httpRequest.getUrl())
                .thenReturn(new URL("https://mobile-api.argenta.be/accounts"));
        mockUnauthorizedResponse();
        // when
        boolean result = objectUnderTest.shouldRetry(httpResponse);
        // then
        Assertions.assertThat(result).isFalse();
    }

    private void mockUnauthorizedResponse() {
        final String body =
                "{\"code\":\"error.unauthorized\",\"message\":\"Unauthorized\",\"reregister\":false}";
        Mockito.when(httpResponse.getStatus()).thenReturn(401);
        Mockito.when(httpResponse.getBody(String.class)).thenReturn(body);
    }
}
