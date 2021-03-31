package se.tink.libraries.http.apache;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.http.ConnectionClosedException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.libraries.simple_http_server.SimpleHTTPServer;

/**
 * If there is an attempt to read the entity of a ClosableHttpResponse after {@link
 * CloseableHttpResponse#close()} is invoked, it will no longer be possible to read the response.
 * This will result in ConnectionClosedException, SocketException or the like. However, it appears
 * that this is true for httpcore 4.4.6 + httpclient 4.5.3, but not for httpcore 4.3.2 + httpclient
 * 4.3.4.
 */
public class CloseableHttpResponseTest {

    private static SimpleHTTPServer proxyServer;
    private static CloseableHttpClient httpClient;

    @BeforeClass
    public static void startServer() throws Exception {
        proxyServer = new SimpleHTTPServer(11111);
        proxyServer.start();
        httpClient = HttpClients.createDefault();
    }

    @AfterClass
    public static void stopServer() throws IOException {
        proxyServer.stop(new CountDownLatch(1));
        httpClient.close();
    }

    @Test
    public void readingResponseBeforeCloseWorks() throws IOException {
        final CloseableHttpResponse response;
        HttpPost post = new HttpPost("http://127.0.0.1:11111");
        try (CloseableHttpResponse r = httpClient.execute(post)) {
            response = r;
            EntityUtils.toByteArray(response.getEntity()); // The entity is read here
            // CloseableHttpResponse#close() is invoked here
        }
    }

    @Test(expected = ConnectionClosedException.class)
    public void readingResponseAfterCloseWorks() throws IOException {
        final CloseableHttpResponse response;
        HttpPost post = new HttpPost("http://127.0.0.1:11111");
        try (CloseableHttpResponse r = httpClient.execute(post)) {
            response = r;
            // CloseableHttpResponse#close() is invoked here
        }
        EntityUtils.toByteArray(response.getEntity()); // The entity is read here
    }
}
