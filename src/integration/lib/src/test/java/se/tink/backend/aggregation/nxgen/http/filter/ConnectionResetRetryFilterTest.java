package se.tink.backend.aggregation.nxgen.http.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.net.ssl.SSLException;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ConnectionResetRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ConnectionResetRetryFilterTest {

    @Test
    public void shouldRetryWhenSslException() {
        Filter finalFilter = mock(Filter.class);
        ConnectionResetRetryFilter filter = new ConnectionResetRetryFilter(3, 100);
        filter.setNext(finalFilter);

        SSLException sslException = new SSLException("");
        HttpClientException httpClientException = new HttpClientException(sslException, null);
        HttpResponse expected = mock(HttpResponse.class);

        when(finalFilter.handle(null))
                .thenThrow(httpClientException)
                .thenThrow(httpClientException)
                .thenReturn(expected);

        HttpResponse actual = filter.handle(null);
        Assert.assertEquals(expected, actual);
    }
}
