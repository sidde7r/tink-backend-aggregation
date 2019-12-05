package se.tink.backend.aggregation.eidassigner;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.http.truststrategy.TrustRootCaStrategy;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;

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

                SSLConnectionSocketFactory sslsf =
                        new SSLConnectionSocketFactory(sslContext, new AllowAllHostnameVerifier());

                PoolingHttpClientConnectionManager connectionManager =
                        new PoolingHttpClientConnectionManager(
                                RegistryBuilder.<ConnectionSocketFactory>create()
                                        .register(
                                                "http",
                                                PlainConnectionSocketFactory.getSocketFactory())
                                        .register("https", sslsf)
                                        .build());
                connectionManager.setDefaultMaxPerRoute(4);
                connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(conf.getHost())), 10);

                ConnectionKeepAliveStrategy ttl = (r, c) -> 60 * 1000;
                httpClient =
                        HttpClients.custom()
                                .setConnectionManager(connectionManager)
                                .setKeepAliveStrategy(ttl)
                                .build();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return httpClient;
    }
}
