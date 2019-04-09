package se.tink.libraries.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJerseyClientFactory {

    private static final int READ_TIMEOUT_MS = 30000;
    private static final int CONNECT_TIMEOUT_MS = 10000;

    protected final Logger log;

    protected AbstractJerseyClientFactory(Class<? extends AbstractJerseyClientFactory> cls) {
        log = LoggerFactory.getLogger(cls);
    }

    public Client createBasicClient() {
        return createBasicClient(null);
    }

    public Client createBasicClient(ObjectMapper mapper) {
        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();

        if (mapper != null) {
            JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
            jacksonProvider.setMapper(mapper);
            clientConfig.getSingletons().add(jacksonProvider);
        }

        Client client = Client.create(clientConfig);
        client.setChunkedEncodingSize(null);
        setTimeouts(client);

        return client;
    }

    private void setTimeouts(Client client) {
        client.setReadTimeout(READ_TIMEOUT_MS);
        client.setConnectTimeout(CONNECT_TIMEOUT_MS);
    }

    public TinkApacheHttpClient4 createCustomClient() {
        return createCustomClient(new DefaultApacheHttpClient4Config());
    }

    public TinkApacheHttpClient4 createCustomClient(
            SSLContext sslContext, X509HostnameVerifier hostnameVerifier) {

        BasicCookieStore cookieStore = new BasicCookieStore();

        CloseableHttpClient apacheClient =
                HttpClientBuilder.create()
                        .setDefaultRequestConfig(RequestConfig.DEFAULT)
                        .setDefaultCookieStore(cookieStore)
                        .setSslcontext(sslContext)
                        .setHostnameVerifier(hostnameVerifier)
                        .build();

        TinkApacheHttpClient4Handler tinkJerseyApacheHttpsClientHandler =
                new TinkApacheHttpClient4Handler(apacheClient, cookieStore, false);

        TinkApacheHttpClient4 client =
                new TinkApacheHttpClient4(tinkJerseyApacheHttpsClientHandler);
        client.setChunkedEncodingSize(null);
        setTimeouts(client);

        return client;
    }

    public TinkApacheHttpClient4 createCustomClient(ApacheHttpClient4Config clientConfig) {
        clientConfig.getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, null);

        TinkApacheHttpClient4 client = TinkApacheHttpClient4.create(clientConfig);

        client.setChunkedEncodingSize(null);
        // client.addFilter(new GZIPContentEncodingFilter(false));
        setTimeouts(client);

        return client;
    }

    public ApacheHttpClient4 createThreadSafeCookieClient() {
        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();

        clientConfig
                .getProperties()
                .put(
                        ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER,
                        new ThreadSafeClientConnManager());

        return createCookieClient(clientConfig);
    }

    public ApacheHttpClient4 createCookieClient() {
        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();

        return createCookieClient(clientConfig);
    }

    public ApacheHttpClient4 createCookieClient(ApacheHttpClient4Config clientConfig) {
        ApacheHttpClient4 client = ApacheHttpClient4.create(clientConfig);
        client.setChunkedEncodingSize(null);
        setTimeouts(client);

        // client.addFilter(new GZIPContentEncodingFilter(false));

        return client;
    }

    public Client createCookieClientWithoutSSL() {
        return createCookieClientWithoutSSL(new DefaultApacheHttpClient4Config());
    }

    public Client createCookieClientWithoutSSL(ApacheHttpClient4Config clientConfig) {
        SSLContext sslContext;
        try {
            sslContext =
                    SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(
                        sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        Registry<ConnectionSocketFactory> r =
                RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).build();

        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        connectionManager
                .getSchemeRegistry()
                .register(
                        new Scheme(
                                "https",
                                443,
                                new SSLSocketFactory(
                                        sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));

        clientConfig
                .getProperties()
                .put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager);

        ApacheHttpClient4 client = ApacheHttpClient4.create(clientConfig);
        client.setChunkedEncodingSize(null);
        setTimeouts(client);

        // client.addFilter(new GZIPContentEncodingFilter(false));

        return client;
    }

    /**
     * An HTTP client that uses a local proxy. Usually used with Charles web debugging proxy. The
     * client constructed here is _not_ for production use.
     *
     * <p>TODO: Make this reuse {@link #createProxyClient(AgentContext, ApacheHttpClient4Config)}.
     *
     * @param context
     * @return
     */
    public Client createProxyClient() {

        ApacheHttpClient4 client = ApacheHttpClient4.create(createProxyClientConfig());
        client.setChunkedEncodingSize(null);
        setTimeouts(client);

        // client.addFilter(new GZIPContentEncodingFilter(false));

        return client;
    }

    public ApacheHttpClient4Config createProxyClientConfig() {
        SSLContext sslContext;
        try {
            sslContext =
                    SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(
                        sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        Registry<ConnectionSocketFactory> r =
                RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).build();

        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        connectionManager
                .getSchemeRegistry()
                .register(
                        new Scheme(
                                "https",
                                443,
                                new SSLSocketFactory(
                                        sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));

        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();
        clientConfig
                .getProperties()
                .put(ApacheHttpClient4Config.PROPERTY_PROXY_URI, "http://127.0.0.1:8888/");
        clientConfig
                .getProperties()
                .put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager);

        return clientConfig;
    }

    /**
     * An HTTP client that uses a local proxy. Usually used with Charles web debugging proxy. The
     * client constructed here is _not_ for production use.
     *
     * @param context
     * @param clientConfig
     * @return
     */
    public Client createProxyClient(ApacheHttpClient4Config clientConfig) {
        SSLContext sslContext;
        try {
            sslContext =
                    SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(
                        sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        Registry<ConnectionSocketFactory> r =
                RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).build();

        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        connectionManager
                .getSchemeRegistry()
                .register(
                        new Scheme(
                                "https",
                                443,
                                new SSLSocketFactory(
                                        sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));

        clientConfig
                .getProperties()
                .put(ApacheHttpClient4Config.PROPERTY_PROXY_URI, "http://127.0.0.1:8888/");
        clientConfig
                .getProperties()
                .put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager);

        ApacheHttpClient4 client = ApacheHttpClient4.create(clientConfig);
        client.setChunkedEncodingSize(null);
        setTimeouts(client);

        // client.addFilter(new GZIPContentEncodingFilter(false));

        return client;
    }

    public Client createNaiveClient() throws Exception {
        // System.setProperty("javax.net.debug", "all");

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts =
                new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
                };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        ClientConfig config = new DefaultClientConfig();
        config.getProperties()
                .put(
                        HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                        new HTTPSProperties((s, sslSession) -> true, sc));

        Client client = Client.create(config);
        setTimeouts(client);

        client.addFilter(new GZIPContentEncodingFilter(false));

        return client;
    }
}
