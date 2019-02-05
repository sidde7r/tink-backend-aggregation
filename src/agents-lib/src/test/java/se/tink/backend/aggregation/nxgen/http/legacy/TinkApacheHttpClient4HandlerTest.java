package se.tink.backend.aggregation.nxgen.http.legacy;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.EOFException;
import java.io.InputStream;
import java.net.URI;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TinkApacheHttpClient4HandlerTest {
    private HttpClient client;

    @Before
    public void setUp() throws Exception {

        InputStream inputStream = Mockito.mock(InputStream.class);
        Mockito.when(inputStream.markSupported()).thenReturn(true);

        Mockito.when(inputStream.available()).thenThrow(new EOFException());
        Mockito.when(inputStream.read()).thenThrow(new EOFException());

        HttpEntity httpEntity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpEntity.getContent()).thenReturn(inputStream);

        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        Mockito.when(httpResponse.getAllHeaders()).thenReturn(new Header[0]);
        Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);

        client = Mockito.mock(HttpClient.class);
        Mockito.when(client.execute(Mockito.any(HttpHost.class), Mockito.any(HttpUriRequest.class),
                Mockito.any(HttpContext.class)))
                .thenReturn(httpResponse);
    }

    @Test
    public void exceptionWhenCallingAvailable() throws Exception {
        URI uri = new URI("https://www.tink.se/kalle");
        HttpHost httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        ClientRequest cr = Mockito.mock(ClientRequest.class);
        Mockito.when(cr.getHeaders()).thenReturn((MultivaluedMap) new MultivaluedMapImpl());
        Mockito.when(cr.getMethod()).thenReturn("GET");
        Mockito.when(cr.getURI()).thenReturn(uri);
        Mockito.when(cr.getEntity()).thenReturn(null);

        TinkApacheHttpClient4Handler handler = new TinkApacheHttpClient4Handler(client);
        ClientResponse response = handler.handle(cr);

        Assert.assertFalse(response.hasEntity());
    }
}