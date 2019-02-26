package se.tink.backend.aggregation.agents.utils.jersey;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.TextUtils;
import se.tink.libraries.net.AbstractJerseyClientFactory;
import se.tink.libraries.net.TinkApacheHttpClient4;
import se.tink.libraries.net.TinkApacheHttpClient4Handler;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class JerseyClientFactory extends AbstractJerseyClientFactory {

    public JerseyClientFactory() {
        super(JerseyClientFactory.class);
    }

    public Client createBasicClient(OutputStream httpLogOutputStream) {

        Client client = createBasicClient();

        try {
            if (httpLogOutputStream != null) {
                client.addFilter(
                        new LoggingFilter(new PrintStream(httpLogOutputStream, true, "UTF-8")));
            }
        } catch (Exception e) {
            log.error("Could not add logging filter", e);
        }

        return client;
    }

    public TinkApacheHttpClient4 createCustomClient(OutputStream httpLogOutputStream) {
        return createCustomClient(httpLogOutputStream, new DefaultApacheHttpClient4Config());
    }

    public TinkApacheHttpClient4 createCustomClient(
            OutputStream httpLogOutputStream, ApacheHttpClient4Config clientConfig) {

        TinkApacheHttpClient4 client = createCustomClient(clientConfig);

        try {
            if (httpLogOutputStream != null) {
                client.addFilter(
                        new LoggingFilter(new PrintStream(httpLogOutputStream, true, "UTF-8")));
            }
        } catch (Exception e) {
            log.error("Could not add logging filter", e);
        }

        return client;
    }

    public ApacheHttpClient4 createCookieClient(OutputStream httpLogOutputStream) {
        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();

        return createCookieClient(httpLogOutputStream, clientConfig);
    }

    public ApacheHttpClient4 createCookieClient(
            OutputStream httpLogOutputStream, ApacheHttpClient4Config clientConfig) {

        ApacheHttpClient4 client = createCookieClient(clientConfig);

        try {
            if (httpLogOutputStream != null) {
                client.addFilter(
                        new LoggingFilter(new PrintStream(httpLogOutputStream, true, "UTF-8")));
            }
        } catch (Exception e) {
            log.error("Could not add logging filter", e);
        }

        return client;
    }

    /**
     * An HTTP client that uses a local proxy. Usually used with Charles web debugging proxy. The
     * client constructed here is _not_ for production use.
     *
     * <p>TODO: Make this reuse {@link #createProxyClient(OutputStream, ApacheHttpClient4Config)}.
     *
     * @param httpLogOutputStream
     * @return
     */
    public Client createProxyClient(OutputStream httpLogOutputStream) {

        Client client = createProxyClient();

        try {
            if (httpLogOutputStream != null) {
                client.addFilter(
                        new LoggingFilter(new PrintStream(httpLogOutputStream, true, "UTF-8")));
            }
        } catch (Exception e) {
            log.error("Could not add logging filter", e);
        }

        return client;
    }

    /**
     * An HTTP client that uses a local proxy. Usually used with Charles web debugging proxy. The
     * client constructed here is _not_ for production use.
     *
     * @param httpLogOutputStream
     * @param clientConfig
     * @return
     */
    public Client createProxyClient(
            OutputStream httpLogOutputStream, ApacheHttpClient4Config clientConfig) {

        Client client = createProxyClient(clientConfig);

        try {
            if (httpLogOutputStream != null) {
                client.addFilter(
                        new LoggingFilter(new PrintStream(httpLogOutputStream, true, "UTF-8")));
            }
        } catch (Exception e) {
            log.error("Could not add logging filter", e);
        }

        return client;
    }

    public Client createNaiveClient(OutputStream httpLogOutputStream) throws Exception {

        Client client = createNaiveClient();

        try {
            if (httpLogOutputStream != null) {
                client.addFilter(
                        new LoggingFilter(new PrintStream(httpLogOutputStream, true, "UTF-8")));
            }
        } catch (Exception e) {
            log.error("Could not add logging filter", e);
        }

        return client;
    }

    public TinkApacheHttpClient4 createClientWithRedirectHandler(OutputStream httpLogOutputStream) {
        return createClientWithRedirectHandler(httpLogOutputStream, createRedirectStrategy());
    }

    public TinkApacheHttpClient4 createClientWithRedirectHandler(
            OutputStream httpLogOutputStream, RedirectStrategy redirectStrategy) {
        BasicCookieStore cookieStore = new BasicCookieStore();
        RequestConfig requestConfig = RequestConfig.custom().build();

        CloseableHttpClient apacheClient =
                HttpClientBuilder.create()
                        .setDefaultRequestConfig(requestConfig)
                        .setDefaultCookieStore(cookieStore)
                        .setRedirectStrategy(redirectStrategy)
                        .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(30000).build())
                        .build();

        TinkApacheHttpClient4Handler tinkJerseyApacheHttpsClientHandler =
                new TinkApacheHttpClient4Handler(apacheClient, cookieStore, false);
        TinkApacheHttpClient4 tinkJerseyClient =
                new TinkApacheHttpClient4(tinkJerseyApacheHttpsClientHandler);

        try {
            tinkJerseyClient.addFilter(
                    new LoggingFilter(new PrintStream(httpLogOutputStream, true, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not add buffered logging filter.");
        }

        tinkJerseyClient.setChunkedEncodingSize(null);
        return tinkJerseyClient;
    }

    /**
     * If Location headers with spaces are received, this method rewrites the urls to avoid
     * URISyntaxException.
     */
    private RedirectStrategy createRedirectStrategy() {
        return new DefaultRedirectStrategy() {
            protected URI createLocationURI(final String location) throws ProtocolException {
                try {
                    final URIBuilder b =
                            new URIBuilder(new URI(location.replace(" ", "%20")).normalize());
                    final String host = b.getHost();
                    if (host != null) {
                        b.setHost(host.toLowerCase(Locale.ENGLISH));
                    }
                    final String path = b.getPath();
                    if (TextUtils.isEmpty(path)) {
                        b.setPath("/");
                    }
                    return b.build();
                } catch (final URISyntaxException e) {
                    throw new ProtocolException("Invalid redirect URI: " + location, e);
                }
            }
        };
    }
}
