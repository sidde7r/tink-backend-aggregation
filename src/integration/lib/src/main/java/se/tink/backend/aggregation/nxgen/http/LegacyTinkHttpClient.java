package se.tink.backend.aggregation.nxgen.http;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import io.vavr.jackson.datatype.VavrModule;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.utils.jersey.LoggingFilter;
import se.tink.backend.aggregation.agents.utils.jersey.interceptor.MessageSignInterceptor;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.LegacyFilterable;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.persistent.Header;
import se.tink.backend.aggregation.nxgen.http.filter.filters.persistent.PersistentHeaderFilter;
import se.tink.backend.aggregation.nxgen.http.handler.HttpResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.hostnameverifier.ProxyHostnameVerifier;
import se.tink.backend.aggregation.nxgen.http.legacy.TinkApacheHttpClient4;
import se.tink.backend.aggregation.nxgen.http.legacy.TinkApacheHttpClient4Handler;
import se.tink.backend.aggregation.nxgen.http.legacy.TinkApacheHttpRequestExecutor;
import se.tink.backend.aggregation.nxgen.http.metrics.MetricFilter;
import se.tink.backend.aggregation.nxgen.http.redirect.ApacheHttpRedirectStrategy;
import se.tink.backend.aggregation.nxgen.http.redirect.DenyAllRedirectHandler;
import se.tink.backend.aggregation.nxgen.http.redirect.FixRedirectHandler;
import se.tink.backend.aggregation.nxgen.http.redirect.handler.RedirectHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.response.jersey.JerseyHttpResponse;
import se.tink.backend.aggregation.nxgen.http.serializecontainer.SerializeContainer;
import se.tink.backend.aggregation.nxgen.http.truststrategy.TrustAllCertificatesStrategy;
import se.tink.backend.aggregation.nxgen.http.truststrategy.TrustRootCaStrategy;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;

/** @deprecated Use {@link NextGenTinkHttpClient} instead. */
@Deprecated
public class LegacyTinkHttpClient extends LegacyFilterable<TinkHttpClient>
        implements TinkHttpClient {

    private final LogMasker logMasker;
    private final LoggingMode loggingMode;
    private TinkApacheHttpRequestExecutor requestExecutor;
    private Client internalClient = null;
    private final ClientConfig internalClientConfig;
    private HttpClientBuilder internalHttpClientBuilder;
    private RequestConfig.Builder internalRequestConfigBuilder;
    private final BasicCookieStore internalCookieStore;
    private SSLContextBuilder internalSslContextBuilder;
    private String userAgent;
    private final AggregatorInfo aggregator;

    private List<String> cipherSuites;

    private boolean followRedirects = false;
    private final ApacheHttpRedirectStrategy redirectStrategy;

    private LoggingFilter debugOutputLoggingFilter;
    private boolean debugOutput = false;

    private final ByteArrayOutputStream logOutputStream;
    private final MetricRegistry metricRegistry;
    private final Provider provider;

    private final Filter finalFilter = new SendRequestFilter();
    private final PersistentHeaderFilter persistentHeaderFilter = new PersistentHeaderFilter();

    private String cookieSpec;
    private static final ImmutableList<String> cookieSpecifications =
            ImmutableList.<String>builder()
                    .add(CookieSpecs.BROWSER_COMPATIBILITY)
                    .add(CookieSpecs.NETSCAPE)
                    .add(CookieSpecs.IGNORE_COOKIES)
                    .add(CookieSpecs.STANDARD)
                    .add(CookieSpecs.BEST_MATCH)
                    .build();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private SSLContext sslContext;
    private MessageSignInterceptor messageSignInterceptor;

    private HttpResponseStatusHandler responseStatusHandler;

    public void setMessageSignInterceptor(MessageSignInterceptor messageSignInterceptor) {
        this.messageSignInterceptor = messageSignInterceptor;
    }

    private class DEFAULTS {
        private static final String DEFAULT_USER_AGENT = CommonHeaders.DEFAULT_USER_AGENT;
        private static final int TIMEOUT_MS = 30000;
        private static final int MAX_REDIRECTS = 10;
        private static final boolean CHUNKED_ENCODING = false;
        private static final boolean FOLLOW_REDIRECTS = true;
        private static final boolean DEBUG_OUTPUT = false;
    }

    private class CONSTANTS {
        private static final String UTF_8_ENCODING = "utf-8";
        private static final String IDENTITY_ENCODING = "identity";
    }

    public String getUserAgent() {
        Preconditions.checkNotNull(userAgent);
        return this.userAgent;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public String getHeaderAggregatorIdentifier() {
        return aggregator.getAggregatorIdentifier();
    }

    @Override
    public HttpResponseStatusHandler getResponseStatusHandler() {
        return responseStatusHandler;
    }

    // This filter is responsible to send the actual http request and MUST be the tail of the chain.
    private class SendRequestFilter extends Filter {

        @Override
        public HttpResponse handle(HttpRequest httpRequest)
                throws HttpClientException, HttpResponseException {
            // Set URI, body and headers for the real request
            WebResource.Builder resource =
                    getInternalClient()
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
                ClientResponse internalResponse =
                        resource.method(httpRequest.getMethod().toString(), ClientResponse.class);
                // `HttpResponse` uses the `ClientResponse` object internally
                return new JerseyHttpResponse(httpRequest, internalResponse);
            } catch (UniformInterfaceException e) {
                throw new HttpResponseException(
                        e, httpRequest, new JerseyHttpResponse(httpRequest, e.getResponse()));
            } catch (ClientHandlerException e) {
                throw new HttpClientException(e, httpRequest);
            }
        }
    }

    /**
     * Takes a logMasker that masks sensitive values from logs, the loggingMode parameter should
     * only be passed with the value LOGGING_MASKER_COVERS_SECRETS if you are 100% certain that the
     * logMasker handles the sensitive values in the provider. use {@link
     * LogMaskerImpl#shouldLog(Provider)} if you can.
     *
     * @param logMasker Masks values from logs.
     * @param loggingMode determines if logs should be outputted at all.
     */
    public LegacyTinkHttpClient(
            @Nullable AggregatorInfo aggregatorInfo,
            @Nullable MetricRegistry metricRegistry,
            @Nullable ByteArrayOutputStream logOutPutStream,
            @Nullable SignatureKeyPair signatureKeyPair,
            @Nullable Provider provider,
            @Nullable LogMasker logMasker,
            LoggingMode loggingMode) {
        this.requestExecutor = new TinkApacheHttpRequestExecutor(signatureKeyPair);
        this.internalClientConfig = new DefaultApacheHttpClient4Config();
        this.internalCookieStore = new BasicCookieStore();
        this.internalRequestConfigBuilder = RequestConfig.custom();
        this.internalHttpClientBuilder =
                HttpClientBuilder.create()
                        .setRequestExecutor(requestExecutor)
                        .setDefaultCookieStore(this.internalCookieStore);

        this.internalSslContextBuilder =
                new SSLContextBuilder().useProtocol("TLSv1.2").setSecureRandom(new SecureRandom());

        this.redirectStrategy = new ApacheHttpRedirectStrategy();
        this.logOutputStream = logOutPutStream;
        this.aggregator =
                Objects.nonNull(aggregatorInfo)
                        ? aggregatorInfo
                        : AggregatorInfo.getAggregatorForTesting();
        this.metricRegistry = metricRegistry;
        this.provider = provider;
        this.logMasker = logMasker;
        this.loggingMode = loggingMode;
        this.debugOutputLoggingFilter =
                new LoggingFilter(new PrintStream(System.out), this.logMasker, this.loggingMode);

        // Add an initial redirect handler to fix any illegal location paths
        addRedirectHandler(new FixRedirectHandler());

        // Add the filter that is responsible to add persistent data to each request
        addFilter(this.persistentHeaderFilter);

        setTimeout(DEFAULTS.TIMEOUT_MS);
        setChunkedEncoding(DEFAULTS.CHUNKED_ENCODING);
        setMaxRedirects(DEFAULTS.MAX_REDIRECTS);
        setFollowRedirects(DEFAULTS.FOLLOW_REDIRECTS);
        setDebugOutput(DEFAULTS.DEBUG_OUTPUT);
        setUserAgent(DEFAULTS.DEFAULT_USER_AGENT);

        registerJacksonModule(new VavrModule());
        responseStatusHandler =
                new DefaultResponseStatusHandler(
                        this.provider != null ? this.provider.getName() : null);
    }

    public LegacyTinkHttpClient() {
        this(null, null, null, null, null, null, LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS);
    }

    public void setResponseStatusHandler(HttpResponseStatusHandler responseStatusHandler) {
        Preconditions.checkNotNull(responseStatusHandler);
        this.responseStatusHandler = responseStatusHandler;
    }

    private void constructInternalClient() {
        try {
            sslContext = this.internalSslContextBuilder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }

        if (!Strings.isNullOrEmpty(this.cookieSpec)) {
            this.internalRequestConfigBuilder.setCookieSpec(this.cookieSpec);
        }

        RequestConfig requestConfig = this.internalRequestConfigBuilder.build();

        if (!this.followRedirects) {
            // Add a redirect handler to deny all redirects.
            addRedirectHandler(new DenyAllRedirectHandler());
        }

        if (Objects.nonNull(cipherSuites)) {
            final Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register(
                                    "https",
                                    new SSLConnectionSocketFactory(
                                            sslContext,
                                            null,
                                            cipherSuites.stream().toArray(String[]::new),
                                            null))
                            .build();

            internalHttpClientBuilder.setConnectionManager(
                    new BasicHttpClientConnectionManager(socketFactoryRegistry));
        }

        HttpResponseInterceptor contentEncodingFixerInterceptor =
                (response, context) -> {
                    org.apache.http.Header contentEncodingHeader =
                            response.getFirstHeader(HTTP.CONTENT_ENCODING);
                    if (contentEncodingHeader != null
                            && contentEncodingHeader
                                    .getValue()
                                    .equalsIgnoreCase(CONSTANTS.UTF_8_ENCODING)) {
                        response.removeHeaders(HTTP.CONTENT_ENCODING);
                        response.addHeader(HTTP.CONTENT_ENCODING, CONSTANTS.IDENTITY_ENCODING);

                        final HttpEntity entity = response.getEntity();
                        org.apache.http.Header ceheader = entity.getContentEncoding();
                        if (CONSTANTS.UTF_8_ENCODING.equals(
                                ceheader.getValue().toLowerCase(Locale.ENGLISH))) {
                            BasicHttpEntity newEntity = new BasicHttpEntity();
                            newEntity.setContent(entity.getContent());
                            newEntity.setContentEncoding(CONSTANTS.IDENTITY_ENCODING);
                            newEntity.setChunked(entity.isChunked());
                            newEntity.setContentLength(entity.getContentLength());
                            newEntity.setContentType(entity.getContentType());
                            response.setEntity(newEntity);
                        }
                    }
                };

        CloseableHttpClient httpClient =
                this.internalHttpClientBuilder
                        .addInterceptorFirst(contentEncodingFixerInterceptor)
                        .setDefaultRequestConfig(requestConfig)
                        .setSslcontext(sslContext)
                        .setRedirectStrategy(this.redirectStrategy)
                        .build();
        //  NOTE:
        //      `TinkApacheHttpClient4Handler` and `TinkApacheHttpClient4` are used because a) the
        // version of
        //      `ApacheHttpClient4Handler` that we use has a bug when it comes to
        // `Transfer-Encoding: chunked`
        //      (we cannot disable it).
        //      and b) to be able to pass along the redirected URIs on to the internal Jersey
        // response.
        //      Todo: Remove these two temporary classes when we upgrade to a newer
        // ApacheHttpClient4 library.
        TinkApacheHttpClient4Handler httpHandler = new TinkApacheHttpClient4Handler(httpClient);
        this.internalClient = new TinkApacheHttpClient4(httpHandler, this.internalClientConfig);

        // Add agent debug `LoggingFilter`, todo: move this into nxgen
        try {
            if (this.logOutputStream != null && this.logMasker != null) {
                this.internalClient.addFilter(
                        new LoggingFilter(
                                new PrintStream(logOutputStream, true, "UTF-8"),
                                logMasker,
                                loggingMode));
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        if (this.metricRegistry != null && this.provider != null) {
            addFilter(new MetricFilter(this.metricRegistry, this.provider));
        }
        if (this.debugOutput) {
            this.internalClient.addFilter(debugOutputLoggingFilter);
        }
        if (messageSignInterceptor != null) {
            this.addFilter(messageSignInterceptor);
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

    public void registerJacksonModule(Module module) {
        synchronized (OBJECT_MAPPER) {
            OBJECT_MAPPER.registerModule(module);
        }

        final JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
        jacksonProvider.setMapper(OBJECT_MAPPER);

        this.internalClientConfig.getSingletons().add(jacksonProvider);
    }

    /**
     * @param cipherSuites A list of cipher suites to be presented to the server at TLS Client Hello
     *     in order of preference, e.g. TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 etc. This might be
     *     necessary if the choice of cipher suite causes the TLS handshake to fail.
     */
    public void setCipherSuites(final List<String> cipherSuites) {
        this.cipherSuites = cipherSuites;
    }

    public void setUserAgent(String userAgent) {
        Preconditions.checkState(this.internalClient == null);
        this.userAgent = userAgent;
        this.internalHttpClientBuilder = this.internalHttpClientBuilder.setUserAgent(userAgent);
    }

    public void setCookieSpec(String cookieSpec) {
        Preconditions.checkArgument(
                cookieSpecifications.contains(cookieSpec),
                "Not supported cookie specification:" + cookieSpec);
        this.cookieSpec = cookieSpec;
    }

    public void disableSignatureRequestHeader() {
        requestExecutor.disableSignatureRequestHeader();
    }

    @Override
    public void disableAggregatorHeader() {
        throw new NotImplementedException("Use NextGenHttpClient instead");
    }

    @Override
    public void setLoggingStrategy(LoggingStrategy loggingStrategy) {
        throw new NotImplementedException("Use NextGenHttpClient instead");
    }

    public void setEidasProxyConfiguration(EidasProxyConfiguration eidasProxyConfiguration) {
        requestExecutor.setEidasProxyConfiguration(eidasProxyConfiguration);
    }

    public void setTimeout(int milliseconds) {
        Preconditions.checkState(this.internalClient == null);
        // Note: Timeout on an initial proxy connection does not work (bug in library)

        // `CoreConnectionPNames.SO_TIMEOUT` is taken from the SEB agent to fix timeout problems.
        this.internalClientConfig
                .getProperties()
                .put(CoreConnectionPNames.SO_TIMEOUT, milliseconds);
        this.internalRequestConfigBuilder =
                this.internalRequestConfigBuilder
                        .setConnectionRequestTimeout(milliseconds)
                        .setConnectTimeout(milliseconds)
                        .setSocketTimeout(milliseconds);
    }

    public void setChunkedEncoding(boolean chunkedEncoding) {
        Preconditions.checkState(this.internalClient == null);
        // `0` == Default chunk size
        // `null` == Don't use chunked encoding
        this.internalClientConfig
                .getProperties()
                .put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, chunkedEncoding ? 0 : null);
    }

    public void setMaxRedirects(int maxRedirects) {
        Preconditions.checkState(this.internalClient == null);
        // Note: You'll get an exception if `maxRedirects` is set to `1` if the target server
        // redirects more than that.
        this.internalRequestConfigBuilder =
                this.internalRequestConfigBuilder
                        .setCircularRedirectsAllowed(true)
                        .setMaxRedirects(maxRedirects);
    }

    public void setFollowRedirects(boolean followRedirects) {
        Preconditions.checkState(this.internalClient == null);
        // These options don't really do anything (bug), it's the redirect strategy that fixes the
        // issue.
        // Let's keep them for reference till the day we upgrade our libraries.
        this.internalClientConfig
                .getProperties()
                .put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, followRedirects);
        this.internalClientConfig
                .getProperties()
                .put(ClientPNames.HANDLE_REDIRECTS, followRedirects);
        this.followRedirects = followRedirects;
    }

    public void disableSslVerification() {
        loadTrustMaterial(null, new TrustAllCertificatesStrategy());
        this.internalHttpClientBuilder =
                this.internalHttpClientBuilder.setHostnameVerifier(new AllowAllHostnameVerifier());
    }

    public void loadTrustMaterial(KeyStore truststore, TrustStrategy trustStrategy) {
        Preconditions.checkState(this.internalClient == null);
        try {
            this.internalSslContextBuilder =
                    this.internalSslContextBuilder.loadTrustMaterial(truststore, trustStrategy);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setSslProtocol(String sslProtocol) {
        Preconditions.checkNotNull(sslProtocol);
        Preconditions.checkState(this.internalClient == null);
        this.internalSslContextBuilder = this.internalSslContextBuilder.useProtocol(sslProtocol);
    }

    private void setSslClientCertificate(KeyStore keyStore) {
        Preconditions.checkState(this.internalClient == null);
        try {
            internalSslContextBuilder.loadKeyMaterial(keyStore, null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setSslClientCertificate(byte[] clientCertificateBytes, String password) {
        Preconditions.checkState(this.internalClient == null);
        ByteArrayInputStream clientCertificateStream =
                new ByteArrayInputStream(clientCertificateBytes);
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(clientCertificateStream, password.toCharArray());

            internalSslContextBuilder.loadKeyMaterial(keyStore, null);
        } catch (KeyStoreException
                | NoSuchProviderException
                | IOException
                | NoSuchAlgorithmException
                | CertificateException
                | UnrecoverableKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    public void trustRootCaCertificate(byte[] jksData, String password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            ByteArrayInputStream jksStream = new ByteArrayInputStream(jksData);
            keyStore.load(jksStream, password.toCharArray());

            TrustRootCaStrategy trustStrategy =
                    TrustRootCaStrategy.createWithFallbackTrust(keyStore);
            internalSslContextBuilder.loadTrustMaterial(keyStore, trustStrategy);
        } catch (KeyStoreException
                | IOException
                | NoSuchAlgorithmException
                | CertificateException e) {
            throw new IllegalStateException(e);
        }
    }

    private void trustRootCaCertificate(KeyStore keyStore) {
        try {
            TrustRootCaStrategy trustStrategy =
                    TrustRootCaStrategy.createWithoutFallbackTrust(keyStore);
            internalSslContextBuilder.loadTrustMaterial(keyStore, trustStrategy);

        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setProxy(String uri) {
        Preconditions.checkState(this.internalClient == null);

        URI u = URI.create(uri);
        HttpHost proxyHost = new HttpHost(u.getHost(), u.getPort(), u.getScheme());
        this.internalHttpClientBuilder = this.internalHttpClientBuilder.setProxy(proxyHost);
    }

    public void setDebugProxy(String uri) {
        setProxy(uri);
        disableSslVerification();
    }

    public void setProductionProxy(String uri, String username, String password) {
        setProxy(uri);
        requestExecutor.setProxyCredentials(username, password);
    }

    public void setEidasIdentity(EidasIdentity eidasIdentity) {
        requestExecutor.setEidasIdentity(eidasIdentity);
    }

    public void setEidasProxy(EidasProxyConfiguration conf) {
        try {
            setEidasClient(conf.toInternalConfig());

            setProxy(conf.getHost());
            requestExecutor.shouldUseEidasProxy();

            this.internalHttpClientBuilder =
                    this.internalHttpClientBuilder.setHostnameVerifier(
                            new ProxyHostnameVerifier(new URI(conf.getHost()).getHost()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(
                    "Could not initialise client certificate for eIDAS proxy", e);
        }
    }

    public void resetInternalClient() {
        internalClient = null;
    }

    public void clearEidasProxy() {
        requestExecutor.setShouldUseEidasProxy(false);
        internalHttpClientBuilder.setProxy(null).setHostnameVerifier(null);
        internalSslContextBuilder =
                new SSLContextBuilder().useProtocol("TLSv1.2").setSecureRandom(new SecureRandom());
    }
    /**
     * @deprecated This should not be used. Use `setEidasProxy` if making proxied requests. Use
     *     `QsealcSigner` if requesting signatures
     */
    @Deprecated
    public void setEidasSign(EidasProxyConfiguration conf) {
        setEidasClient(conf.toInternalConfig());
    }

    private void setEidasClient(InternalEidasProxyConfiguration conf) {
        try {
            trustRootCaCertificate(conf.getRootCaTrustStore());
            setSslClientCertificate(conf.getClientCertKeystore());
        } catch (KeyStoreException
                | NoSuchProviderException
                | IOException
                | CertificateException
                | NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Error configuring client certificate for eIDAS proxy", e);
        }
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

    public void setCensorSensitiveHeaders(final boolean censorSensitiveHeadersEnabled) {
        debugOutputLoggingFilter =
                new LoggingFilter(
                        new PrintStream(System.out),
                        logMasker,
                        censorSensitiveHeadersEnabled,
                        loggingMode);
    }

    // --- Configuration ---

    // +++ Cookies +++
    public List<Cookie> getCookies() {
        return this.internalCookieStore.getCookies();
    }

    public void addCookie(Cookie... cookies) {
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
        return new LegacyRequestBuilder(
                this,
                this.finalFilter,
                url,
                getHeaderAggregatorIdentifier(),
                responseStatusHandler);
    }

    public <T> T request(Class<T> c, HttpRequest request)
            throws HttpClientException, HttpResponseException {
        return new LegacyRequestBuilder(
                        this,
                        this.finalFilter,
                        getHeaderAggregatorIdentifier(),
                        responseStatusHandler)
                .raw(c, request);
    }

    public void request(HttpRequest request) throws HttpClientException, HttpResponseException {
        new LegacyRequestBuilder(
                        this,
                        this.finalFilter,
                        getHeaderAggregatorIdentifier(),
                        responseStatusHandler)
                .raw(request);
    }
    // --- Requests ---
}
