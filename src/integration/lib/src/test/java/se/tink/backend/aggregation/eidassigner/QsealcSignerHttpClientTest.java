package se.tink.backend.aggregation.eidassigner;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Files;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.libraries.simple_http_server.SimpleHTTPServer;

public class QsealcSignerHttpClientTest {

    private static InternalEidasProxyConfiguration configuration;
    private static SimpleHTTPServer proxyServer;

    @BeforeClass
    public static void startupProxyWithTestConfiguration() throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        configuration =
                Utils.getConfiguration("data/test/qsealc/test-configuration.yaml", EidasProxy.class)
                        .toInternalConfig();

        proxyServer = new SimpleHTTPServer(11111);
        proxyServer.configureSsl(Utils.buildSslContextFactoryFromConfig(), 12345, false, 8 * 1024);
        proxyServer.addContext("/", new ProxyServerHandler());
        proxyServer.addContext("/test", new ProxyServerHandler());
        proxyServer.addContext("/jwt-rsa-sha256", new ProxyServerHandler());
        proxyServer.start();
    }

    @AfterClass
    public static void stopProxy() throws Exception {
        proxyServer.stop(new CountDownLatch(1));
    }

    @Test
    public void qsealcSignerHttpClientTest() throws IOException {
        Assert.assertEquals("development", configuration.getEnvironment());
        Assert.assertNull(QsealcSignerHttpClient.httpClient);
        Assert.assertNotNull(QsealcSignerHttpClient.qsealcSignerHttpClient);

        QsealcSignerHttpClient httpClientGetFirst = QsealcSignerHttpClient.create(configuration);
        QsealcSignerHttpClient httpClientGetSecond = QsealcSignerHttpClient.create(configuration);
        Assert.assertEquals(httpClientGetFirst, httpClientGetSecond);
        Assert.assertNotNull(QsealcSignerHttpClient.httpClient);
        Assert.assertNotNull(QsealcSignerHttpClient.qsealcSignerHttpClient);

        QsealcSignerHttpClient httpClient = QsealcSignerHttpClient.create(configuration);
        HttpPost post = new HttpPost("http://127.0.0.1:11111/test/");
        CloseableHttpResponse response = httpClient.execute(post);
        int httpStatusCode = response.getStatusLine().getStatusCode();
        response.close();
        assertThat(httpStatusCode).isEqualTo(HttpStatus.SC_OK);

        HttpPost postHttps = new HttpPost("https://127.0.0.1:12345/test/");
        CloseableHttpResponse responseHttps = httpClient.execute(postHttps);
        int httpsStatusCode = responseHttps.getStatusLine().getStatusCode();
        responseHttps.close();
        assertThat(httpsStatusCode).isEqualTo(HttpStatus.SC_OK);

        QsealcSigner signer =
                QsealcSignerImpl.build(
                        configuration,
                        QsealcAlg.EIDAS_JWT_RSA_SHA256,
                        new EidasIdentity("", "", "", ""));
        String result = signer.getJWSToken("".getBytes());
        Assert.assertEquals("signature", result);
    }

    private static class ProxyServerHandler extends AbstractHandler {
        @Override
        public void handle(
                String s,
                Request request,
                HttpServletRequest httpServletRequest,
                HttpServletResponse httpServletResponse)
                throws IOException, ServletException {
            httpServletResponse.setStatus(200);
            IOUtils.write(
                    Base64.getEncoder().encode("signature".getBytes()),
                    httpServletResponse.getOutputStream());
            request.setHandled(true);
        }
    }

    private static class Utils {
        private static <T> T getConfiguration(String fileName, Class<T> cls)
                throws FileNotFoundException {
            FileInputStream configFileStream = new FileInputStream(new File(fileName));

            Representer representer = new Representer();

            Yaml yaml = new Yaml(new Constructor(cls), representer);

            return cls.cast(yaml.load(configFileStream));
        }

        private static SslContextFactory buildSslContextFactoryFromConfig()
                throws IOException, KeyStoreException, CertificateException,
                        NoSuchAlgorithmException {
            final SslContextFactory sslContextFactory = new SslContextFactory();

            sslContextFactory.setKeyStore(buildServerKeyStore("changeme"));
            sslContextFactory.setKeyStorePassword("changeme");

            KeyStore clientTrustStore = buildClientTrustStore();

            sslContextFactory.setNeedClientAuth(false);

            sslContextFactory.setTrustStore(clientTrustStore);
            sslContextFactory.setEndpointIdentificationAlgorithm(null);
            return sslContextFactory;
        }

        private static KeyStore buildServerKeyStore(String password)
                throws IOException, KeyStoreException, CertificateException,
                        NoSuchAlgorithmException {

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, password.toCharArray());
            List<Certificate> certificateChain = new ArrayList<>();
            certificateChain.add(getCertificateFromPath("data/test/qsealc/proxytls.crt"));
            Certificate[] certificateChainArray = certificateChain.toArray(new Certificate[0]);
            keyStore.setKeyEntry(
                    "clientcert",
                    getPrivateKeyFromPath("data/test/qsealc/proxytls.key"),
                    password.toCharArray(),
                    certificateChainArray);

            return keyStore;
        }

        private static Certificate getCertificateFromPath(String path) throws IOException {
            return getCertificateFromBytes(Files.toByteArray(new File(path)));
        }

        private static Certificate getCertificateFromBytes(byte[] certificate) {
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

                return certificateFactory.generateCertificate(
                        new ByteArrayInputStream(certificate));

            } catch (CertificateException ex) {
                throw new IllegalArgumentException(
                        "Could not parse certificate: " + ex.getMessage(), ex);
            }
        }

        private static PrivateKey getPrivateKeyFromPath(String keyPath) throws IOException {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(keyPath))) {
                return getPrivateKeyFromReader(reader);
            }
        }

        private static PrivateKey getPrivateKeyFromReader(Reader reader) throws IOException {
            try (PEMParser pemReader = new PEMParser(reader)) {

                Object pemObject = pemReader.readObject();
                PrivateKeyInfo privateKeyInfo;
                if (pemObject instanceof PEMKeyPair) {
                    privateKeyInfo = ((PEMKeyPair) pemObject).getPrivateKeyInfo();
                } else if (pemObject instanceof PrivateKeyInfo) {
                    privateKeyInfo = (PrivateKeyInfo) pemObject;
                } else {
                    throw new IllegalArgumentException(
                            "Private key not in expected format. Got "
                                    + (pemObject != null
                                            ? pemObject.getClass().getCanonicalName()
                                            : "null pemObject"));
                }

                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                return converter.getPrivateKey(privateKeyInfo);
            }
        }

        private static KeyStore buildClientTrustStore()
                throws KeyStoreException, IOException, NoSuchAlgorithmException,
                        CertificateException {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(null, "changeme".toCharArray());
            Certificate certificate = getCertificateFromPath("data/test/qsealc/ca.crt");
            trustStore.setCertificateEntry("cert-0", certificate);
            return trustStore;
        }
    }

    public static class EidasProxy {
        @JsonProperty private String host;
        @JsonProperty private String caPath;
        @JsonProperty private String tlsCrtPath;
        @JsonProperty private String tlsKeyPath;
        @JsonProperty private String environment;
        @JsonProperty private boolean localEidasDev;

        public EidasProxy() {}

        InternalEidasProxyConfiguration toInternalConfig() {
            return new InternalEidasProxyConfiguration(
                    host, caPath, tlsCrtPath, tlsKeyPath, environment, localEidasDev);
        }

        public String getHost() {
            return host;
        }

        public String getCaPath() {
            return caPath;
        }

        public String getTlsCrtPath() {
            return tlsCrtPath;
        }

        public String getTlsKeyPath() {
            return tlsKeyPath;
        }

        public String getEnvironment() {
            return environment;
        }

        public boolean isLocalEidasDev() {
            return localEidasDev;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setCaPath(String caPath) {
            this.caPath = caPath;
        }

        public void setTlsCrtPath(String tlsCrtPath) {
            this.tlsCrtPath = tlsCrtPath;
        }

        public void setTlsKeyPath(String tlsKeyPath) {
            this.tlsKeyPath = tlsKeyPath;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public void setLocalEidasDev(boolean localEidasDev) {
            this.localEidasDev = localEidasDev;
        }
    }
}
