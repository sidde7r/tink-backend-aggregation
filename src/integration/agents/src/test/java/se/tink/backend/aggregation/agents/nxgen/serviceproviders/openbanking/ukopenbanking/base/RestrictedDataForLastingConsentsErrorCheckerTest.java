package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import static org.junit.Assert.*;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class RestrictedDataForLastingConsentsErrorCheckerTest {

    private HttpResponseException httpResponseException;
    private HttpResponse response;
    private HttpRequest request;

    @Before
    public void init() {
        httpResponseException = Mockito.mock(HttpResponseException.class);
        response = Mockito.mock(HttpResponse.class);
        request = Mockito.mock(HttpRequest.class);
        Mockito.when(httpResponseException.getRequest()).thenReturn(request);
        Mockito.when(httpResponseException.getResponse()).thenReturn(response);
    }

    @Test
    public void shouldReturnTrueForRestrictedData() {
        // given
        URL url = URL.of("http://bankdomain.com/accounts/sjkddaha76534/party");
        Mockito.when(request.getUrl()).thenReturn(url);
        Mockito.when(response.getStatus()).thenReturn(403);
        // when
        boolean result =
                RestrictedDataForLastingConsentsErrorChecker.isRestrictedDataLastingConsentsError(
                        httpResponseException);
        // then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void shouldReturnFalseForAllowedData() {
        // given
        URL allowedUrl1 = URL.of("http://bankdomain.com/accounts");
        URL allowedUrl2 = URL.of("http://bankdomain.com/accounts/sjkddaha76534");
        URL allowedUrl3 = URL.of("http://bankdomain.com/accounts/sjkddaha76534/balances");
        URL allowedUrl4 = URL.of("http://bankdomain.com/accounts/sjkddaha76534/transactions");
        Mockito.when(request.getUrl())
                .thenReturn(allowedUrl1)
                .thenReturn(allowedUrl2)
                .thenReturn(allowedUrl3)
                .thenReturn(allowedUrl4);
        Mockito.when(response.getStatus()).thenReturn(403);
        // when
        boolean result1 =
                RestrictedDataForLastingConsentsErrorChecker.isRestrictedDataLastingConsentsError(
                        httpResponseException);
        boolean result2 =
                RestrictedDataForLastingConsentsErrorChecker.isRestrictedDataLastingConsentsError(
                        httpResponseException);
        boolean result3 =
                RestrictedDataForLastingConsentsErrorChecker.isRestrictedDataLastingConsentsError(
                        httpResponseException);
        boolean result4 =
                RestrictedDataForLastingConsentsErrorChecker.isRestrictedDataLastingConsentsError(
                        httpResponseException);
        // then
        Assertions.assertThat(result1).isFalse();
        Assertions.assertThat(result2).isFalse();
        Assertions.assertThat(result3).isFalse();
        Assertions.assertThat(result4).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenResponseStatusInNot403() {
        // given
        URL url = URL.of("http://bankdomain.com/accounts/sjkddaha76534/party");
        Mockito.when(request.getUrl()).thenReturn(url);
        Mockito.when(response.getStatus()).thenReturn(404);
        // when
        boolean result =
                RestrictedDataForLastingConsentsErrorChecker.isRestrictedDataLastingConsentsError(
                        httpResponseException);
        // then
        Assertions.assertThat(result).isFalse();
    }
}
