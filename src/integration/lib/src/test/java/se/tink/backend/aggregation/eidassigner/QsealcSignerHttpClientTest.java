package se.tink.backend.aggregation.eidassigner;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.libraries.simple_http_server.SimpleHTTPServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Security;
import java.util.concurrent.CountDownLatch;

public class QsealcSignerHttpClientTest {

    private static InternalEidasProxyConfiguration configuration;
    static SimpleHTTPServer proxyServer;

    @BeforeClass
    public static void startupProxyWithTestConfiguration() throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        configuration =
                getConfiguration("data/test/qsealc/test-configuration.yaml", EidasProxy.class)
                        .toInternalConfig();
        proxyServer = new SimpleHTTPServer(12345);
        proxyServer.addContext("/", new ProxyServerHandler());
        proxyServer.addContext("/test", new ProxyServerHandler());
        proxyServer.start();
    }

    @AfterClass
    public static void stopProxy() throws Exception {
        proxyServer.stop(new CountDownLatch(1));
    }

    @Test
    public void qsealcSignerHttpClientTest() throws Exception {
        HttpClient httpClient = QsealcSignerHttpClient.getHttpClient(configuration);
        HttpPost post = new HttpPost("http://127.0.0.1:12345/test/");
        HttpResponse response = httpClient.execute(post);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertEquals("development", configuration.getEnvironment());
    }

    static <T> T getConfiguration(String fileName, Class<T> cls) throws FileNotFoundException {
        FileInputStream configFileStream = new FileInputStream(new File(fileName));

        Representer representer = new Representer();

        Yaml yaml = new Yaml(new Constructor(cls), representer);

        return cls.cast(yaml.load(configFileStream));
    }

    static class ProxyServerHandler extends AbstractHandler {
        @Override
        public void handle(
                String s,
                Request request,
                HttpServletRequest httpServletRequest,
                HttpServletResponse httpServletResponse)
                throws IOException, ServletException {
            httpServletResponse.setStatus(200);
            request.setHandled(true);
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

        public InternalEidasProxyConfiguration toInternalConfig() {
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
