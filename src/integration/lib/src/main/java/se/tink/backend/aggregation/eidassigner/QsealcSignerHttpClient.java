package se.tink.backend.aggregation.eidassigner;

import java.security.KeyStore;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.http.truststrategy.TrustRootCaStrategy;

public class QsealcSignerHttpClient {
    private static HttpClient httpClient;

    static synchronized HttpClient getHttpClient(InternalEidasProxyConfiguration conf) {
        if (httpClient == null) {
            try {
                KeyStore trustStore = conf.getRootCaTrustStore();
                KeyStore keyStore = conf.getClientCertKeystore();
                SSLContext sslContext =
                        new SSLContextBuilder()
                                .loadTrustMaterial(
                                        trustStore,
                                        TrustRootCaStrategy.createWithoutFallbackTrust(trustStore))
                                .loadKeyMaterial(keyStore, "changeme".toCharArray())
                                .build();

                httpClient =
                        HttpClients.custom()
                                .setHostnameVerifier(new AllowAllHostnameVerifier())
                                .setSslcontext(sslContext)
                                .setConnectionManager(
                                        new PoolingHttpClientConnectionManager(
                                                60, TimeUnit.SECONDS))
                                .build();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return httpClient;
    }
}
