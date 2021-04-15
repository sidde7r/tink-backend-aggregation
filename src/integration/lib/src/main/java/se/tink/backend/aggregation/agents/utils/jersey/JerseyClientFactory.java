package se.tink.backend.aggregation.agents.utils.jersey;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
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
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.libraries.net.client.TinkApacheHttpClient4;
import se.tink.libraries.net.client.factory.AbstractJerseyClientFactory;
import se.tink.libraries.net.client.handler.TinkApacheHttpClient4Handler;

public class JerseyClientFactory extends AbstractJerseyClientFactory {

    private final LogMasker logMasker;
    private final LoggingMode loggingMode;

    /**
     * Takes a logMasker that masks sensitive values from logs, the loggingMode parameter should *
     * only be passed with the value LOGGING_MASKER_COVERS_SECRETS if you are 100% certain that the
     * * logMasker handles the sensitive values in the provider. use {@link *
     * se.tink.backend.aggregation.logmasker.LogMasker#shouldLog(Provider)} if you can.
     *
     * @param logMasker masks values from logs.
     * @param loggingMode determines if logs should be produced at all.
     */
    public JerseyClientFactory(LogMasker logMasker, LoggingMode loggingMode) {
        super(JerseyClientFactory.class);
        this.logMasker = logMasker;
        this.loggingMode = loggingMode;
    }

    public Client createBasicClient(OutputStream httpLogOutputStream) {

        Client client = createBasicClient();
        addLoggingFilter(httpLogOutputStream, client);

        return client;
    }

    public TinkApacheHttpClient4 createCustomClient(OutputStream httpLogOutputStream) {
        return createCustomClient(httpLogOutputStream, new DefaultApacheHttpClient4Config());
    }

    public TinkApacheHttpClient4 createCustomClient(
            OutputStream httpLogOutputStream, ApacheHttpClient4Config clientConfig) {

        TinkApacheHttpClient4 client = createCustomClient(clientConfig);
        addLoggingFilter(httpLogOutputStream, client);

        return client;
    }

    public ApacheHttpClient4 createCookieClient(OutputStream httpLogOutputStream) {
        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();

        return createCookieClient(httpLogOutputStream, clientConfig);
    }

    public ApacheHttpClient4 createCookieClient(
            OutputStream httpLogOutputStream, ApacheHttpClient4Config clientConfig) {

        ApacheHttpClient4 client = createCookieClient(clientConfig);
        addLoggingFilter(httpLogOutputStream, client);

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
                    new LoggingFilter(
                            new PrintStream(httpLogOutputStream, true, "UTF-8"),
                            logMasker,
                            loggingMode));
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

    public void addLoggingFilter(OutputStream httpLogOutputStream, Client client) {
        try {
            if (httpLogOutputStream != null) {
                client.addFilter(
                        new LoggingFilter(
                                new PrintStream(httpLogOutputStream, true, "UTF-8"),
                                logMasker,
                                loggingMode));
            }
        } catch (Exception e) {
            log.error("Could not add logging filter", e);
        }
    }
}
