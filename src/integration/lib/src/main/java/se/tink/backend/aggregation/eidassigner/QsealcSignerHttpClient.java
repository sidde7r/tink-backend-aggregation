package se.tink.backend.aggregation.eidassigner;

import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
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
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
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
                connectionManager.setMaxTotal(40);
                connectionManager.setDefaultMaxPerRoute(5);
                connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(conf.getHost())), 32);

                ConnectionKeepAliveStrategy ttl =
                        (response, context) -> {
                            HeaderElementIterator it =
                                    new BasicHeaderElementIterator(
                                            response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                            while (it.hasNext()) {
                                HeaderElement he = it.nextElement();
                                String param = he.getName();
                                String value = he.getValue();
                                if (value != null && "timeout".equalsIgnoreCase(param)) {
                                    return Long.parseLong(value) * 1000;
                                }
                            }
                            return 600 * 1000;
                        };

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
