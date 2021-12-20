package se.tink.libraries.jersey.utils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import eu.geekplace.javapinning.pin.Pin;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import se.tink.libraries.net.client.TinkApacheHttpClient4;
import se.tink.libraries.net.client.handler.TinkApacheHttpClient4Handler;

// The client built by this factory must be used when communicating from AND to the
// aggregation cluster.
@Slf4j
public class InterClusterJerseyClientFactory {
    private static final int READ_TIMEOUT_MS = (int) TimeUnit.MINUTES.toMillis(10);
    private static final int CONNECT_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(10);

    private final HttpClientBuilder internalHttpClientBuilder;
    private final SSLContextBuilder internalSslContextBuilder;
    private final RequestConfig.Builder internalRequestConfigBuilder;
    private final ClientConfig internalClientConfig;

    private boolean doCompressRequests = true;
    private int readTimeoutMs = READ_TIMEOUT_MS;
    private int connectTimeoutMs = CONNECT_TIMEOUT_MS;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public InterClusterJerseyClientFactory(final ClientConfig config) {
        this.internalHttpClientBuilder = HttpClientBuilder.create();

        this.internalSslContextBuilder =
                new SSLContextBuilder().useProtocol("TLSv1.2").setSecureRandom(new SecureRandom());

        this.internalRequestConfigBuilder = RequestConfig.custom();

        this.internalClientConfig = config;

        // `0` == Default chunk size
        // `null` == Don't use chunked encoding
        this.internalClientConfig
                .getProperties()
                .put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, 0);
    }

    public InterClusterJerseyClientFactory withClientCertificate(
            byte[] clientCertificateBytes, String password) {
        ByteArrayInputStream clientCertificateStream =
                new ByteArrayInputStream(clientCertificateBytes);
        try {
            Instant start = Instant.now();
            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(clientCertificateStream, password.toCharArray());

            internalSslContextBuilder.loadKeyMaterial(
                    keyStore, null); // the keyStore doesn't have a PW.
            log.info("Build KeyStore in {}ms", Duration.between(start, Instant.now()).toMillis());
            return this;
        } catch (KeyStoreException
                | NoSuchProviderException
                | IOException
                | NoSuchAlgorithmException
                | CertificateException
                | UnrecoverableKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    public InterClusterJerseyClientFactory withServerCertificatePinning(Collection<Pin> pins) {
        try {
            internalSslContextBuilder.loadTrustMaterial(
                    null, new PinServerCertificateTrustStrategy(pins));
            return this;
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    public InterClusterJerseyClientFactory disableRequestCompression() {
        this.doCompressRequests = false;
        return this;
    }

    public InterClusterJerseyClientFactory disableHostNameVerification() {
        internalHttpClientBuilder.setHostnameVerifier(
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return this;
    }

    public InterClusterJerseyClientFactory setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
        return this;
    }

    public InterClusterJerseyClientFactory setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        return this;
    }

    public Client build() {
        Instant start = Instant.now();
        SSLContext sslContext;
        try {
            sslContext = internalSslContextBuilder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }
        log.info("Build SSL context in {}ms", Duration.between(start, Instant.now()).toMillis());

        RequestConfig reguestConfig = this.internalRequestConfigBuilder.build();

        start = Instant.now();
        CloseableHttpClient httpClient =
                internalHttpClientBuilder
                        .setSslcontext(sslContext)
                        .setDefaultRequestConfig(reguestConfig)
                        .build();
        log.info("Build HttpClient in {}ms", Duration.between(start, Instant.now()).toMillis());

        TinkApacheHttpClient4Handler httpHandler =
                new TinkApacheHttpClient4Handler(httpClient, new BasicCookieStore(), false);

        Client client = new TinkApacheHttpClient4(httpHandler, this.internalClientConfig);
        client.setReadTimeout(readTimeoutMs);
        client.setConnectTimeout(connectTimeoutMs);
        client.addFilter(new GZIPContentEncodingFilter(doCompressRequests));

        return client;
    }
}
