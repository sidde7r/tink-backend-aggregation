package se.tink.backend.aggregation.agents.abnamro.client;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.WebResource.Builder;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.core.MediaType;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.jersey.JerseyClientFactory;
import se.tink.backend.aggregation.configuration.integrations.abnamro.TrustStoreConfiguration;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.libraries.net.BasicJerseyClientFactory;

public abstract class Client {

    protected Logger log;
    private final String hostname;
    private com.sun.jersey.api.client.Client client;

    protected Client(
            Class<? extends Client> cls,
            JerseyClientFactory clientFactory,
            OutputStream logOutputStream,
            TrustStoreConfiguration trustStoreConfiguration,
            String hostname) {
        this.log = LoggerFactory.getLogger(cls);
        this.hostname = hostname;

        SSLContext sslContext;

        try {
            sslContext = createSslContext(trustStoreConfiguration);
        } catch (Exception e) {
            throw new RuntimeException("Could not initiate client", e);
        }

        X509HostnameVerifier hostnameVerifier = new CustomHostnameVerifier(hostname);

        this.client =
                new BasicJerseyClientFactory().createCustomClient(sslContext, hostnameVerifier);
        clientFactory.addLoggingFilter(logOutputStream, this.client);
    }

    protected Builder createClientRequest(String path) {
        return client.resource("https://" + hostname + path)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("User-Agent", CommonHeaders.DEFAULT_USER_AGENT);
    }

    protected static SSLContext createSslContext(TrustStoreConfiguration trustStoreConfiguration)
            throws Exception {
        return createSslContext(
                trustStoreConfiguration.getPath(),
                trustStoreConfiguration.getPassword().toCharArray());
    }

    protected static SSLContext createSslContext(String trustStorePath, char[] trustStorePassword)
            throws Exception {

        KeyStore trustStore = KeyStore.getInstance("jks");
        trustStore.load(new FileInputStream(trustStorePath), trustStorePassword);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);

        return createSslContext(trustManagerFactory);
    }

    private static SSLContext createSslContext(TrustManagerFactory trustManagerFactory)
            throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    class CustomHostnameVerifier implements X509HostnameVerifier {

        private final String hostname;

        public CustomHostnameVerifier(String hostname) {
            this.hostname = hostname;
        }

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return verify(hostname);
        }

        @Override
        public void verify(String hostname, SSLSocket socket) throws IOException {
            if (!verify(hostname)) {
                throw new IOException("Invalid hostname.");
            }
        }

        @Override
        public void verify(String hostname, X509Certificate certificate) throws SSLException {
            if (!verify(hostname)) {
                throw new SSLException("Invalid hostname.");
            }
        }

        @Override
        public void verify(String hostname, String[] cns, String[] subjectAlts)
                throws SSLException {
            if (!verify(hostname)) {
                throw new SSLException("Invalid hostname.");
            }
        }

        public boolean verify(String hostname) {
            if (Strings.isNullOrEmpty(this.hostname)) {
                return true;
            } else {
                return this.hostname.equals(hostname);
            }
        }
    }
}
