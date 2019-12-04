package se.tink.backend.aggregation.QsealcSigner;

import com.google.common.io.Files;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import se.tink.backend.aggregation.agents.utils.crypto.parser.Pem;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;

public class QsealcSignerHttpClientTest {

    @BeforeClass
    public static void startupProxyWithTestConfiguration() throws Exception {

        InternalEidasProxyConfiguration configuration =
                InternalEidasProxyConfiguration.getConfiguration(
                        "src/tink-integration-eidas-proxy/etc/test-configuration.yaml",
                        InternalEidasProxyConfiguration.class);

        Server server = new Server(new InetSocketAddress(12345));
        ContextHandler ctx = new ContextHandler();
        ctx.setContextPath("/");
        ctx.setHandler(new ProxyServerHandler());
        server.setHandler(ctx);
        final HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.setSecurePort(12443);


        final SslContextFactory sslContextFactory = new SslContextFactory();
        String keyStorePassword = generateRandomKeystorePassword();
        KeyStore trustStore = getRootCaTrustStore();
        KeyStore keyStore = getClientCertKeystore();
        sslContextFactory.setKeyStore(
                keyStore);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setNeedClientAuth(true);
        sslContextFactory.setTrustStore(trustStore);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        final HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer(false));
        final ServerConnector httpsConnector =
                new ServerConnector(
                        server,
                        new SslConnectionFactory(
                                sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                        new HttpConnectionFactory(httpsConfiguration));
        httpsConnector.setPort(12443);
        server.addConnector(httpsConnector);
        server.start();


    }

    @Test
    public void qsealcSignerHttpClientTest() {

    }

    public static <T> T getConfiguration(String fileName, Class<T> cls) throws FileNotFoundException {
        FileInputStream configFileStream = new FileInputStream(new File(fileName));

        Representer representer = new Representer();

        Yaml yaml = new Yaml(new Constructor(cls), representer);

        return cls.cast(yaml.load(configFileStream));
    }

    private static KeyStore getRootCaTrustStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        final Certificate certificate;
        certificate = Pem.parseCertificate(Files.toByteArray(new File("")));
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, "changeme".toCharArray());
        keyStore.setCertificateEntry("changeme", certificate);
        return keyStore;
    }

    private static KeyStore getClientCertKeystore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException {
            Certificate certificate = Pem.parseCertificate(Files.toByteArray(new File("")));
            PrivateKey privateKey = Pem.parsePrivateKey(Files.toByteArray(new File("")));
            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(null, "password".toCharArray());
            Certificate[] certificateChain = new Certificate[1];
            certificateChain[0] = certificate;
            keyStore.setKeyEntry("clientcert", privateKey, null, certificateChain);
            return keyStore;
    }

    private static String generateRandomKeystorePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    static class ProxyServerHandler extends AbstractHandler {

        @Override
        public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {

        }
    }
}
