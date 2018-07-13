package se.tink.backend.aggregation.nxgen.http;

import com.google.common.base.Preconditions;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.CoreConnectionPNames;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.utils.jersey.LoggingFilter;
import se.tink.backend.aggregation.cluster.identification.Aggregator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.Filterable;
import se.tink.backend.aggregation.nxgen.http.legacy.TinkApacheHttpClient4;
import se.tink.backend.aggregation.nxgen.http.legacy.TinkApacheHttpClient4Handler;
import se.tink.backend.aggregation.nxgen.http.legacy.TinkApacheHttpRequestExecutor;
import se.tink.backend.aggregation.nxgen.http.metrics.MetricFilter;
import se.tink.backend.aggregation.nxgen.http.persistent.Header;
import se.tink.backend.aggregation.nxgen.http.persistent.PersistentHeaderFilter;
import se.tink.backend.aggregation.nxgen.http.redirect.ApacheHttpRedirectStrategy;
import se.tink.backend.aggregation.nxgen.http.redirect.DenyAllRedirectHandler;
import se.tink.backend.aggregation.nxgen.http.redirect.FixRedirectHandler;
import se.tink.backend.aggregation.nxgen.http.redirect.RedirectHandler;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.libraries.jersey.utils.InterClusterJerseyClientFactory;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TinkHttpClient extends Filterable<TinkHttpClient> {

    private Client internalClient = null;
    private final ClientConfig internalClientConfig;
    private HttpClientBuilder internalHttpClientBuilder;
    private RequestConfig.Builder internalRequestConfigBuilder;
    private final BasicCookieStore internalCookieStore;
    private SSLContextBuilder internalSslContextBuilder;
    private String userAgent;
    private final Aggregator aggregator;

    private boolean followRedirects = false;
    private final ApacheHttpRedirectStrategy redirectStrategy;

    private final LoggingFilter debugOutputLoggingFilter= new LoggingFilter(new PrintStream(System.out));
    private boolean debugOutput = false;

    private final AgentContext context;
    private final Credentials credentials;

    private final Filter finalFilter = new SendRequestFilter();
    private final PersistentHeaderFilter persistentHeaderFilter = new PersistentHeaderFilter();

    private class DEFAULTS {

        private final static String UNKNOWN_AGGREGATOR = "Tink (+https://www.tink.se/; noc@tink.se)";
        private final static int TIMEOUT_MS = 30000;
        private final static int MAX_REDIRECTS = 10;
        private final static boolean CHUNKED_ENCODING = false;
        private final static boolean FOLLOW_REDIRECTS = true;
        private final static boolean DEBUG_OUTPUT = false;
    }

    public String getUserAgent() {
        if (this.userAgent == null) {
            return aggregator.getAggregatorIdentifier();
        }

        return this.userAgent;
    }

    public String getHeaderAggregatorIdentifier(){
        if(aggregator != null){
            return aggregator.getAggregatorIdentifier();
        }

        return DEFAULTS.UNKNOWN_AGGREGATOR;
    }

    // good site to test this: https://badssl.com/
    private class TrustAllCertificatesStrategy implements TrustStrategy {

        @Override
        public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            return true;
        }
    }
    // This filter is responsible to send the actual http request and MUST be the tail of the chain.
    private class SendRequestFilter extends Filter {

        @Override
        public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
            // Set URI, body and headers for the real request
            WebResource.Builder resource = getInternalClient()
                                                        .resource(httpRequest.getURI())
                                                        .entity(httpRequest.getBody());

            MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
            if (headers != null) {
                for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
                    for (Object value : e.getValue()) {
                        resource = resource.header(e.getKey(), value);
                    }
                }
            }

            try {
                // Make actual http request
                ClientResponse internalResponse = resource.method(
                                                            httpRequest.getMethod().toString(),
                                                            ClientResponse.class);
                // `HttpResponse` uses the `ClientResponse` object internally
                return new HttpResponse(httpRequest, internalResponse);
            } catch(UniformInterfaceException e) {
                throw new HttpResponseException(e, httpRequest, new HttpResponse(httpRequest, e.getResponse()));
            } catch (ClientHandlerException e) {
                throw new HttpClientException(e, httpRequest);
            }
        }
    }
    public TinkHttpClient(@Nullable AgentContext context, @Nullable Credentials credentials) {
        this.context = context;
        this.credentials = credentials;

        this.internalClientConfig = new DefaultApacheHttpClient4Config();
        this.internalCookieStore = new BasicCookieStore();
        this.internalRequestConfigBuilder = RequestConfig.custom();
        this.internalHttpClientBuilder = HttpClientBuilder.create()
                                        .setRequestExecutor(new TinkApacheHttpRequestExecutor())
                                        .setDefaultCookieStore(this.internalCookieStore);

        this.internalSslContextBuilder = new SSLContextBuilder()
                .useProtocol("TLSv1.2")
                .setSecureRandom(new SecureRandom());

        this.redirectStrategy = new ApacheHttpRedirectStrategy();

        // Add an initial redirect handler to fix any illegal location paths
        addRedirectHandler(new FixRedirectHandler());

        // Add the filter that is responsible to add persistent data to each request
        addFilter(this.persistentHeaderFilter);

        this.aggregator = (context == null) ? new Aggregator(DEFAULTS.UNKNOWN_AGGREGATOR) : context.getAggregator();

        setUserAgent(aggregator.getAggregatorIdentifier());
        setTimeout(DEFAULTS.TIMEOUT_MS);
        setChunkedEncoding(DEFAULTS.CHUNKED_ENCODING);
        setMaxRedirects(DEFAULTS.MAX_REDIRECTS);
        setFollowRedirects(DEFAULTS.FOLLOW_REDIRECTS);
        setDebugOutput(DEFAULTS.DEBUG_OUTPUT);
        addPersistentHeader("X-Aggregator", getHeaderAggregatorIdentifier());
    }

    private void constructInternalClient() {
        SSLContext sslContext;
        try {
            sslContext = this.internalSslContextBuilder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }

        RequestConfig reguestConfig = this.internalRequestConfigBuilder.build();

        if (!this.followRedirects) {
            // Add a redirect handler to deny all redirects.
            addRedirectHandler(new DenyAllRedirectHandler());
        }

        CloseableHttpClient httpClient = this.internalHttpClientBuilder
                                            .setDefaultRequestConfig(reguestConfig)
                                            .setSslcontext(sslContext)
                                            .setRedirectStrategy(this.redirectStrategy)
                                            .build();
        //  NOTE:
        //      `TinkApacheHttpClient4Handler` and `TinkApacheHttpClient4` are used because a) the version of
        //      `ApacheHttpClient4Handler` that we use has a bug when it comes to `Transfer-Encoding: chunked`
        //      (we cannot disable it).
        //      and b) to be able to pass along the redirected URIs on to the internal Jersey response.
        //      Todo: Remove these two temporary classes when we upgrade to a newer ApacheHttpClient4 library.
        TinkApacheHttpClient4Handler httpHandler = new TinkApacheHttpClient4Handler(httpClient);
        this.internalClient = new TinkApacheHttpClient4(httpHandler, this.internalClientConfig);

        // Add agent debug `LoggingFilter`, todo: move this into nxgen
        if (this.context != null) {
            try {
                if (this.context.getLogOutputStream() != null) {
                    this.internalClient.addFilter(
                            new LoggingFilter(
                                    new PrintStream(
                                            this.context.getLogOutputStream(),
                                            true,
                                            "UTF-8")));
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
            if (this.context instanceof AgentWorkerContext) {
                addFilter(new MetricFilter((AgentWorkerContext) this.context));
            }
        }
        if (this.debugOutput) {
            this.internalClient.addFilter(debugOutputLoggingFilter);
        }
    }

    public Client getInternalClient() {
        if (this.internalClient == null) {
            constructInternalClient();
        }
        return this.internalClient;
    }

    // +++ Configuration +++
    public void addMessageReader(MessageBodyReader<?> messageBodyReader) {
        this.internalClientConfig.getSingletons().add(messageBodyReader);
    }
    public void addMessageWriter(MessageBodyWriter<?> messageBodyWriter) {
        this.internalClientConfig.getSingletons().add(messageBodyWriter);
    }

    public void setUserAgent(String userAgent) {
        Preconditions.checkState(this.internalClient == null);
        this.userAgent = userAgent;
        this.internalHttpClientBuilder = this.internalHttpClientBuilder.setUserAgent(userAgent);
    }

    public void setTimeout(int milliseconds) {
        Preconditions.checkState(this.internalClient == null);
        // Note: Timeout on an initial proxy connection does not work (bug in library)

        // `CoreConnectionPNames.SO_TIMEOUT` is taken from the SEB agent to fix timeout problems.
        this.internalClientConfig.getProperties().put(CoreConnectionPNames.SO_TIMEOUT, milliseconds);
        this.internalRequestConfigBuilder = this.internalRequestConfigBuilder
                                            .setConnectionRequestTimeout(milliseconds)
                                            .setConnectTimeout(milliseconds)
                                            .setSocketTimeout(milliseconds);
    }

    public void setChunkedEncoding(boolean chunkedEncoding) {
        Preconditions.checkState(this.internalClient == null);
        // `0` == Default chunk size
        // `null` == Don't use chunked encoding
        this.internalClientConfig.getProperties().put(
                            ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE,
                            chunkedEncoding ? 0 : null);
    }

    public void setMaxRedirects(int maxRedirects) {
        Preconditions.checkState(this.internalClient == null);
        // Note: You'll get an exception if `maxRedirects` is set to `1` if the target server redirects more than that.
        this.internalRequestConfigBuilder = this.internalRequestConfigBuilder
                                            .setCircularRedirectsAllowed(true)
                                            .setMaxRedirects(maxRedirects);
    }

    public void setFollowRedirects(boolean followRedirects) {
        Preconditions.checkState(this.internalClient == null);
        // These options don't really do anything (bug), it's the redirect strategy that fixes the issue.
        // Let's keep them for reference till the day we upgrade our libraries.
        this.internalClientConfig.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, followRedirects);
        this.internalClientConfig.getProperties().put(ClientPNames.HANDLE_REDIRECTS, followRedirects);
        this.followRedirects = followRedirects;
    }

    public void disableSslVerification() {
        loadTrustMaterial(null, new TrustAllCertificatesStrategy());
    }

    public void loadTrustMaterial(KeyStore truststore, TrustAllCertificatesStrategy trustStrategy) {
        Preconditions.checkState(this.internalClient == null);
        try {
            this.internalSslContextBuilder = this.internalSslContextBuilder.loadTrustMaterial(
                    truststore,
                    trustStrategy);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setSslProtocol(String sslProtocol) {
        Preconditions.checkNotNull(sslProtocol);
        Preconditions.checkState(this.internalClient == null);
        this.internalSslContextBuilder = this.internalSslContextBuilder.useProtocol(sslProtocol);
    }

    public void setSslClientCertificate(byte[] clientCertificateBytes, String password) {
        Preconditions.checkState(this.internalClient == null);
        ByteArrayInputStream clientCertificateStream = new ByteArrayInputStream(clientCertificateBytes);
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(clientCertificateStream, password.toCharArray());

            internalSslContextBuilder.loadKeyMaterial(keyStore, null);
        } catch (KeyStoreException | NoSuchProviderException | IOException | NoSuchAlgorithmException |
                CertificateException | UnrecoverableKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setProxy(String uri) {
        Preconditions.checkState(this.internalClient == null);
        URI u = URI.create(uri);
        HttpHost proxyHost = new HttpHost(u.getHost(), u.getPort(), u.getScheme());
        this.internalHttpClientBuilder = this.internalHttpClientBuilder.setProxy(proxyHost);

        disableSslVerification();
    }

    public void addRedirectHandler(RedirectHandler handler) {
        this.redirectStrategy.addHandler(handler);
    }

    public void setDebugOutput(boolean debugOutput) {
        this.debugOutput = debugOutput;

        if (internalClient == null) {
            return;
        }

        if (debugOutput && !internalClient.isFilterPresent(debugOutputLoggingFilter)) {
            this.internalClient.addFilter(debugOutputLoggingFilter);
        } else if (!debugOutput && internalClient.isFilterPresent(debugOutputLoggingFilter)) {
            this.internalClient.removeFilter(debugOutputLoggingFilter);
        }
    }
    // --- Configuration ---

    // +++ Cookies +++
    public List<Cookie> getCookies() {
        return this.internalCookieStore.getCookies();
    }

    public void addCookie(Cookie ...cookies) {
        this.internalCookieStore.addCookies(cookies);
    }

    public void clearCookies() {
        this.internalCookieStore.clear();
    }

    public void clearExpiredCookies(Date date) {
        this.internalCookieStore.clearExpired(date);
    }

    public void clearExpiredCookies() {
        clearExpiredCookies(new Date());
    }
    // --- Cookies ---

    // +++ Persistent request data +++
    public void addPersistentHeader(String key, String value) {
        this.persistentHeaderFilter.addHeader(new Header(key, value));
    }

    public void clearPersistentHeaders() {
        this.persistentHeaderFilter.clearHeaders();
    }

    public boolean isPersistentHeaderPresent(String headerKey) {
        return this.persistentHeaderFilter.isHeaderPresent(headerKey);
    }
    // --- Persistent request data ---

    // +++ Serialization +++
    // Serialize/deserialize the following data:
    //  - Cookies
    //  - PersistentHeaders
    public String serialize() {
        SerializeContainer serializeContainer = new SerializeContainer();
        serializeContainer.setCookies(this.internalCookieStore.getCookies());
        serializeContainer.setHeaders(this.persistentHeaderFilter.getHeaders());
        return SerializationUtils.serializeToString(serializeContainer);
    }

    public void initialize(SerializeContainer serializeContainer) {

        this.internalCookieStore.addCookies(serializeContainer.getCookies().toArray(new Cookie[0]));
        this.persistentHeaderFilter.setHeaders(serializeContainer.getHeaders());
    }
    // --- Serialization ---

    // +++ Requests +++
    public RequestBuilder request(String url) {
        return request(new URL(url));
    }

    public RequestBuilder request(URL url) {
        return new RequestBuilder(this, this.finalFilter, url);
    }

    public <T> T request(Class<T> c, HttpRequest request) throws HttpClientException, HttpResponseException {
        return new RequestBuilder(this, this.finalFilter).raw(c, request);
    }

    public void request(HttpRequest request) throws HttpClientException, HttpResponseException {
        new RequestBuilder(this, this.finalFilter).raw(request);
    }
    // --- Requests ---
}
