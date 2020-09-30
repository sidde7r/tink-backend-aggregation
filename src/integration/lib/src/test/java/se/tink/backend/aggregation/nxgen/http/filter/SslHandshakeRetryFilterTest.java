package se.tink.backend.aggregation.nxgen.http.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.net.ssl.SSLHandshakeException;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.SslHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SslHandshakeRetryFilterTest {

    @Test
    public void shouldRetryWhenSslHandshakeException() {
        Filter finalFilter = mock(Filter.class);
        SslHandshakeRetryFilter filter = new SslHandshakeRetryFilter(2, 0);
        filter.setNext(finalFilter);

        SSLHandshakeException sslHandshakeException = new SSLHandshakeException("");
        HttpClientException httpClientException =
                new HttpClientException(sslHandshakeException, null);
        HttpResponse expected = mock(HttpResponse.class);

        when(finalFilter.handle(null))
                .thenThrow(httpClientException)
                .thenThrow(httpClientException)
                .thenReturn(expected);

        HttpResponse actual = filter.handle(null);
        Assert.assertEquals(expected, actual);
    }
}
