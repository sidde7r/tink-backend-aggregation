package se.tink.backend.aggregation.nxgen.http;

import static java.util.Collections.singletonList;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.io.IOException;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
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
import org.apache.http.conn.ssl.StrictHostnameVerifier;
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
import se.tink.backend.aggregation.agents.utils.jersey.ResponseLoggingFilter;
import se.tink.backend.aggregation.agents.utils.jersey.interceptor.MessageSignInterceptor;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.client.LoggingScope;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.event.configuration.RawBankDataEventCreationStrategies;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.DenyAlwaysRawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.RawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventAccumulator;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventProducer;
import se.tink.backend.aggregation.nxgen.http.event.interceptor.RawBankDataEventProducerInterceptor;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.NextGenFilterable;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.executiontime.ExecutionTimeLoggingFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.executiontime.TimeMeasuredRequestExecutor;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.persistent.Header;
import se.tink.backend.aggregation.nxgen.http.filter.filters.persistent.PersistentHeaderFilter;
import se.tink.backend.aggregation.nxgen.http.handler.HttpResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.hostnameverifier.ProxyHostnameVerifier;
import se.tink.backend.aggregation.nxgen.http.legacy.TinkApacheHttpClient4;
import se.tink.backend.aggregation.nxgen.http.legacy.TinkApacheHttpClient4Handler;
import se.tink.backend.aggregation.nxgen.http.legacy.TinkApacheHttpRequestExecutor;
import se.tink.backend.aggregation.nxgen.http.log.adapter.DefaultApacheRequestLoggingAdapter;
import se.tink.backend.aggregation.nxgen.http.log.adapter.DefaultJerseyResponseLoggingAdapter;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingExecutor;
import se.tink.backend.aggregation.nxgen.http.log.executor.aap.HttpAapLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.aap.HttpAapLoggingExecutor;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.HttpJsonLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.HttpJsonLoggingExecutor;
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
import se.tink.backend.aggregation.nxgen.http.truststrategy.VerifyHostname;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class NextGenTinkHttpClient extends NextGenFilterable<TinkHttpClient>
        implements TinkHttpClient {

    private TinkApacheHttpRequestExecutor requestExecutor;
    private Client internalClient = null;
    private final ClientConfig internalClientConfig;
    private HttpClientBuilder internalHttpClientBuilder;
    private RequestConfig.Builder internalRequestConfigBuilder;
    private final BasicCookieStore internalCookieStore;
    private SSLContextBuilder internalSslContextBuilder;
    private String userAgent;
    private final AggregatorInfo aggregator;
    private boolean shouldAddAggregatorHeader = true;

    private List<String> cipherSuites;

    private boolean followRedirects = false;
    private final ApacheHttpRedirectStrategy redirectStrategy;

    private final LogMasker logMasker;
    private final LoggingMode loggingMode;
    private LoggingStrategy loggingStrategy = LoggingStrategy.DEFAULT;
    private List<LoggingScope> loggingScopes = singletonList(LoggingScope.HTTP_AAP);
    private final HttpAapLogger httpAapLogger;
    private final HttpJsonLogger httpJsonLogger;

    private final MetricRegistry metricRegistry;
    private final Provider provider;

    private final PersistentHeaderFilter persistentHeaderFilter = new PersistentHeaderFilter();
    private ExecutionTimeLoggingFilter executionTimeLoggingFilter;

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

    private HttpResponseStatusHandler responseStatusHandler;

    private RawBankDataEventProducerInterceptor rawBankDataEventProducerInterceptor;
    private RawBankDataEventProducer rawBankDataEventProducer;

    private static class DEFAULTS {
        private static final String DEFAULT_USER_AGENT = CommonHeaders.DEFAULT_USER_AGENT;
        private static final int TIMEOUT_MS = 30000;
        private static final int MAX_REDIRECTS = 10;
        private static final boolean CHUNKED_ENCODING = false;
        private static final boolean FOLLOW_REDIRECTS = true;
    }

    private static class CONSTANTS {
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
    @FilterOrder(category = FilterPhases.SEND, order = Integer.MAX_VALUE)
    private class SendRequestFilter extends Filter {

        @Override
        public HttpResponse handle(HttpRequest httpRequest)
                throws HttpClientException, HttpResponseException {
            HttpJsonLogger.beforeHttpExchange();

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
            } finally {
                HttpJsonLogger.afterHttpExchange();
            }
        }
    }

    /**
     * Takes a logMasker that masks sensitive values from logs, the shouldLog parameter should only
     * * be passed with the value LOGGING_MASKER_COVERS_SECRETS if you are 100% certain that the *
     * logMasker handles the sensitive values in the provider. use {@link *
     * se.tink.backend.aggregation.logmasker.LogMasker#shouldLog(Provider)} if you can.
     *
     * @param builder the builder.
     * @param logMasker Masks values from logs.
     * @param loggingMode determines if logs should be outputted at all.
     */
    private NextGenTinkHttpClient(
            final Builder builder, LogMasker logMasker, LoggingMode loggingMode) {
        this.requestExecutor = new TinkApacheHttpRequestExecutor(builder.getSignatureKeyPair());

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
        this.httpAapLogger = builder.getHttpAapLogger();
        this.httpJsonLogger = builder.getHttpJsonLogger();
        this.aggregator =
                Objects.nonNull(builder.getAggregatorInfo())
                        ? builder.getAggregatorInfo()
                        : AggregatorInfo.getAggregatorForTesting();
        this.metricRegistry = builder.getMetricRegistry();
        this.provider = builder.getProvider();

        // Add an initial redirect handler to fix any illegal location paths
        addRedirectHandler(new FixRedirectHandler());

        // Add the filter that is responsible to add persistent data to each request
        addFilter(this.persistentHeaderFilter);
        setTimeout(DEFAULTS.TIMEOUT_MS);
        setChunkedEncoding(DEFAULTS.CHUNKED_ENCODING);
        setMaxRedirects(DEFAULTS.MAX_REDIRECTS);
        setFollowRedirects(DEFAULTS.FOLLOW_REDIRECTS);
        setUserAgent(DEFAULTS.DEFAULT_USER_AGENT);

        registerJacksonModule(new VavrModule());
        registerJacksonModule(new JavaTimeModule());
        responseStatusHandler =
                new DefaultResponseStatusHandler(
                        this.provider != null ? this.provider.getName() : null);
        this.logMasker = logMasker;
        this.loggingMode = loggingMode;

        this.executionTimeLoggingFilter =
                new ExecutionTimeLoggingFilter(TimeMeasuredRequestExecutor::withRequest);
        addFilter(executionTimeLoggingFilter);
        addFilter(new SendRequestFilter());

        // Build raw bank data event emission interceptor
        this.rawBankDataEventProducer = builder.getRawBankDataEventProducer();
        RawBankDataEventAccumulator rawBankDataEventAccumulator =
                builder.getRawBankDataEventAccumulator();
        String correlationId = builder.getCorrelationId();
        if (Objects.nonNull(this.rawBankDataEventProducer)
                && Objects.nonNull(rawBankDataEventAccumulator)
                && Objects.nonNull(correlationId)) {
            this.rawBankDataEventProducerInterceptor =
                    new RawBankDataEventProducerInterceptor(
                            rawBankDataEventProducer,
                            rawBankDataEventAccumulator,
                            correlationId,
                            new DenyAlwaysRawBankDataEventCreationTriggerStrategy());
            addFilter(this.rawBankDataEventProducerInterceptor);
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
     * @return the builder.
     */
    public static NextGenTinkHttpClient.Builder builder(
            LogMasker logMasker, LoggingMode loggingMode) {
        return new Builder(logMasker, loggingMode);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static final class Builder {

        private final LogMasker logMasker;
        private final LoggingMode loggingMode;
        private HttpAapLogger httpAapLogger;
        private HttpJsonLogger httpJsonLogger;

        private AggregatorInfo aggregatorInfo;
        private MetricRegistry metricRegistry;
        private SignatureKeyPair signatureKeyPair;
        private Provider provider;

        private RawBankDataEventProducer rawBankDataEventProducer;
        private RawBankDataEventAccumulator rawBankDataEventAccumulator;
        private String correlationId;

        public Builder(LogMasker logMasker, LoggingMode loggingMode) {
            this.logMasker = logMasker;
            this.loggingMode = loggingMode;
        }

        public NextGenTinkHttpClient build() {
            return new NextGenTinkHttpClient(this, logMasker, loggingMode);
        }

        public Builder setRawBankDataEventEmissionComponents(
                RawBankDataEventProducer rawBankDataEventProducer,
                RawBankDataEventAccumulator rawBankDataEventAccumulator,
                String correlationId) {
            this.rawBankDataEventProducer = rawBankDataEventProducer;
            this.rawBankDataEventAccumulator = rawBankDataEventAccumulator;
            this.correlationId = correlationId;
            return this;
        }
    }

    public void setResponseStatusHandler(HttpResponseStatusHandler responseStatusHandler) {
        Preconditions.checkNotNull(responseStatusHandler);
        this.responseStatusHandler = responseStatusHandler;
    }

    public void setRequestExecutionTimeLogger(
            Function<HttpRequest, TimeMeasuredRequestExecutor> measureRequestTimeExecution) {
        Preconditions.checkNotNull(executionTimeLoggingFilter);
        removeFilter(executionTimeLoggingFilter);
        this.executionTimeLoggingFilter =
                new ExecutionTimeLoggingFilter(measureRequestTimeExecution);
        addFilter(executionTimeLoggingFilter);
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
        setupLogging();

        if (this.metricRegistry != null && this.provider != null) {
            addFilter(new MetricFilter(this.metricRegistry, this.provider));
        }
    }

    private void setupLogging() {
        if (loggingStrategy == LoggingStrategy.DEFAULT) {
            setupDefaultLogging();
            return;
        }
        if (loggingStrategy == LoggingStrategy.EXPERIMENTAL) {
            setupExperimentalLogging();
            return;
        }
        if (loggingStrategy == LoggingStrategy.DISABLED) {
            log.info("Default logging disabled");
            return;
        }
        log.warn("Unexpected logging strategy: {}", loggingStrategy);
    }

    private void setupDefaultLogging() {
        if (httpAapLogger == null) {
            log.warn("Could not create logging filter - AAP logger not configured");
            return;
        }
        internalClient.addFilter(new LoggingFilter(httpAapLogger, logMasker, loggingMode));
    }

    private void setupExperimentalLogging() {
        List<LoggingExecutor> loggingExecutors =
                loggingScopes.stream()
                        .map(this::createLoggingExecutorForScope)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        if (loggingExecutors.isEmpty()) {
            log.warn("Did not create any logging executors");
            return;
        }

        DefaultApacheRequestLoggingAdapter apacheLoggingAdapter =
                new DefaultApacheRequestLoggingAdapter(loggingExecutors);
        requestExecutor.setRequestLoggingAdapter(apacheLoggingAdapter);

        DefaultJerseyResponseLoggingAdapter jerseyResponseLoggingAdapter =
                new DefaultJerseyResponseLoggingAdapter(loggingExecutors);
        internalClient.addFilter(new ResponseLoggingFilter(jerseyResponseLoggingAdapter));
    }

    private Optional<LoggingExecutor> createLoggingExecutorForScope(LoggingScope loggingScope) {
        if (loggingScope == LoggingScope.HTTP_AAP) {
            return createAppLoggingExecutor();
        }
        if (loggingScope == LoggingScope.HTTP_JSON) {
            return createJsonLoggingExecutor();
        }
        log.warn("Unexpected logging scope: {}", loggingScope);
        return Optional.empty();
    }

    private Optional<LoggingExecutor> createAppLoggingExecutor() {
        if (httpAapLogger == null) {
            log.warn("Could not create AAP logging executor - logger not configured");
            return Optional.empty();
        }
        return Optional.of(new HttpAapLoggingExecutor(httpAapLogger, logMasker, loggingMode));
    }

    private Optional<LoggingExecutor> createJsonLoggingExecutor() {
        if (httpJsonLogger == null) {
            log.warn("Could not create JSON logging executor - logger not configured");
            return Optional.empty();
        }
        return Optional.of(new HttpJsonLoggingExecutor(httpJsonLogger));
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

    @Override
    public Provider getProvider() {
        return provider;
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
        this.shouldAddAggregatorHeader = false;
    }

    @Override
    public void setLoggingStrategy(LoggingStrategy loggingStrategy) {
        this.loggingStrategy = loggingStrategy;
    }

    @Override
    public void setLoggingScopes(LoggingScope... loggingScopes) {
        this.loggingScopes = ImmutableList.copyOf(loggingScopes);
    }

    @Override
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
            if (trustStrategy instanceof VerifyHostname) {
                this.internalHttpClientBuilder =
                        this.internalHttpClientBuilder.setHostnameVerifier(
                                new StrictHostnameVerifier());
            }
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

    public void setMessageSignInterceptor(MessageSignInterceptor messageSignInterceptor) {
        addFilter(messageSignInterceptor);
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
        final RequestBuilder builder =
                new NextGenRequestBuilder(
                        new ArrayList<>(this.getFilters()),
                        url,
                        getHeaderAggregatorIdentifier(),
                        responseStatusHandler);
        if (!shouldAddAggregatorHeader) {
            builder.removeAggregatorHeader();
        }

        return builder;
    }

    public <T> T request(Class<T> c, HttpRequest request)
            throws HttpClientException, HttpResponseException {
        return new NextGenRequestBuilder(
                        this.getFilters(), getHeaderAggregatorIdentifier(), responseStatusHandler)
                .raw(c, request);
    }

    public void request(HttpRequest request) throws HttpClientException, HttpResponseException {
        new NextGenRequestBuilder(
                        this.getFilters(), getHeaderAggregatorIdentifier(), responseStatusHandler)
                .raw(request);
    }
    // --- Requests ---

    // +++ Raw bank data event emission +++
    @Override
    public void overrideRawBankDataEventCreationStrategies(
            RawBankDataEventCreationStrategies configuration) {
        if (Objects.nonNull(this.rawBankDataEventProducer)) {
            this.rawBankDataEventProducer.overrideRawBankDataEventCreationStrategies(configuration);
        }
    }

    @Override
    public void overrideRawBankDataEventCreationTriggerStrategy(
            RawBankDataEventCreationTriggerStrategy configuration) {
        if (Objects.nonNull(this.rawBankDataEventProducerInterceptor)) {
            this.rawBankDataEventProducerInterceptor
                    .overrideRawBankDataEventCreationTriggerStrategy(configuration);
        }
    }
    // --- Raw bank data event emission ---
}
