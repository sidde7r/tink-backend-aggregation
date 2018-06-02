package se.tink.backend.product.execution.unit.agents.sbab.mortgage;

import com.sun.jersey.api.client.Client;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
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
import se.tink.backend.product.execution.log.ProductExecutionLogger;
import se.tink.libraries.net.TinkApacheHttpClient4;
import se.tink.libraries.net.TinkApacheHttpClient4Handler;

public class JerseyClientFactory {
    private static final ProductExecutionLogger log = new ProductExecutionLogger(JerseyClientFactory.class);

    public Client createClientWithRedirectHandler() {
        return createClientWithRedirectHandler(createRedirectStrategy());
    }

    public TinkApacheHttpClient4 createClientWithRedirectHandler(RedirectStrategy redirectStrategy) {
        BasicCookieStore cookieStore = new BasicCookieStore();
        RequestConfig requestConfig = RequestConfig.custom().build();

        CloseableHttpClient apacheClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .setRedirectStrategy(redirectStrategy)
                .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(30000).build())
                .build();

        TinkApacheHttpClient4Handler tinkJerseyApacheHttpsClientHandler = new TinkApacheHttpClient4Handler(
                apacheClient, cookieStore, false);
        TinkApacheHttpClient4 tinkJerseyClient = new TinkApacheHttpClient4(tinkJerseyApacheHttpsClientHandler);

        //TODO: (Phase 2) add our own logging filter

        tinkJerseyClient.setChunkedEncodingSize(null);
        return tinkJerseyClient;
    }

    /**
     * If Location headers with spaces are received, this method rewrites the urls to avoid URISyntaxException.
     */
    private RedirectStrategy createRedirectStrategy() {
        return new DefaultRedirectStrategy() {
            protected URI createLocationURI(final String location) throws ProtocolException {
                try {
                    final URIBuilder b = new URIBuilder(new URI(location.replace(" ", "%20")).normalize());
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
