package se.tink.libraries.jersey.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import eu.geekplace.javapinning.JavaPinning;
import eu.geekplace.javapinning.pin.Pin;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import se.tink.libraries.net.TinkApacheHttpClient4Handler;

public class InterContainerJerseyClientFactory {
    // These should rarely change and the passphrase isn't sensitive. Therefore, not configurable
    // from
    // AggregationServiceConfiguration.
    private static final String DEFAULT_INTER_CONTAINER_TRUSTSTORE_PASSPHRACE = "changeme";
    private static final String DEFAULT_INTER_CONTAINER_TRUSTSTORE_PATH =
            "data/security/TinkCA.truststore";

    private static final int READ_TIMEOUT_MS = (int) TimeUnit.MINUTES.toMillis(10);
    private static final int CONNECT_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(10);
    private static final AllowAnyHost ANY_HOST_NAME_VERIFIER = new AllowAnyHost();

    private DefaultClientConfig config;
    private int readTimeoutMs = READ_TIMEOUT_MS;
    private int connectTimeoutMs = CONNECT_TIMEOUT_MS;

    /**
     * Making this a method call to make it explicit that pinning is disabled. Should not avoided in
     * production.
     */
    public static InterContainerJerseyClientFactory withoutPinning() {
        return new InterContainerJerseyClientFactory();
    }

    /** See {@link #withoutPinning()}. */
    private InterContainerJerseyClientFactory() {
        this(ImmutableSet.of());
    }

    public InterContainerJerseyClientFactory(String pinnedCertificate) {
        this(ImmutableSet.of(pinnedCertificate));
    }

    // See
    // https://github.com/Flowdalic/java-pinning#https-and-other-services-using-tls-right-from-the-start and
    // https://github.com/Flowdalic/java-pinning/issues/3#issuecomment-151826014 on how to extract
    // these.
    public InterContainerJerseyClientFactory(Collection<String> pinnedCertificatesStrings) {
        ImmutableSet<Pin> pinnedCertificates =
                ImmutableSet.copyOf(
                        Iterables.transform(pinnedCertificatesStrings, Pin::fromString));

        config = new DefaultClientConfig();

        final SSLContext ctx;

        if (pinnedCertificates.isEmpty()) {

            // If we don't do pinning (above), we need to trust the certificate we talk to. How? By
            // browsing hundreds of
            // internet pages on instantiating an SSLContext. Jokes aside - we instantiate an
            // SSLContext that only
            // accepts certificates signed by our CA. It's worth pointing out that we still ignore
            // host checking of the
            // cert because our discovery is IP based.
            try {

                KeyStore localTrustStore = KeyStore.getInstance("JKS");

                InputStream in = new FileInputStream(DEFAULT_INTER_CONTAINER_TRUSTSTORE_PATH);
                localTrustStore.load(
                        in, DEFAULT_INTER_CONTAINER_TRUSTSTORE_PASSPHRACE.toCharArray());

                TrustManagerFactory tmf =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(localTrustStore);

                ctx = SSLContext.getInstance("TLS");
                ctx.init(null, tmf.getTrustManagers(), null);
            } catch (NoSuchAlgorithmException
                    | KeyManagementException
                    | KeyStoreException
                    | CertificateException
                    | IOException e) {
                throw new RuntimeException("Could not instantiate a the SSLContext.", e);
            }

        } else {

            // Setup TLS certificate pinning and verification.
            try {
                ctx = JavaPinning.forPins(pinnedCertificates);
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                throw new RuntimeException("Could not pin certificate.", e);
            }
        }

        // We allow any hosts here since we aren't using hosts for service discovery hosts.
        // !!! Important we don't disable host checking unless we have certificates to pin !!!
        config.getProperties()
                .put(
                        HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                        new HTTPSProperties(ANY_HOST_NAME_VERIFIER, ctx));
    }

    public Client buildWithoutSslVerification() {
        try {
            SSLContext sslContext =
                    new SSLContextBuilder()
                            .useProtocol("TLSv1.2")
                            .setSecureRandom(new SecureRandom())

                            // Do not verify the server's certificate
                            .loadTrustMaterial(null, new TrustAllCertificatesStrategy())
                            .build();

            CloseableHttpClient httpClient =
                    HttpClientBuilder.create()
                            .setSslcontext(sslContext)

                            // Do not verify the hostname of the server's certificate
                            .setHostnameVerifier(
                                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                            .build();

            Client client =
                    new Client(
                            new TinkApacheHttpClient4Handler(
                                    httpClient, new BasicCookieStore(), false));
            client.setReadTimeout(readTimeoutMs);
            client.setConnectTimeout(connectTimeoutMs);

            return client;
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }
    }

    public Client build() {

        // Instantiating the client we are going to use.

        Client client = Client.create(config);

        // Set additional Jersey client properties.

        client.setReadTimeout(readTimeoutMs);
        client.setConnectTimeout(connectTimeoutMs);

        return client;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }
}

class AllowAnyHost implements HostnameVerifier {

    @Override
    public boolean verify(String arg0, SSLSession arg1) {
        return true;
    }
}

class TrustAllCertificatesStrategy implements TrustStrategy {

    @Override
    public boolean isTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {
        return true;
    }
}
