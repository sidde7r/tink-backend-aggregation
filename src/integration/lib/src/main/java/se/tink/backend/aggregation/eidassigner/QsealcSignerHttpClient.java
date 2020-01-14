package se.tink.backend.aggregation.eidassigner;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.http.truststrategy.TrustRootCaStrategy;
import se.tink.libraries.metrics.collection.MetricCollector;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.types.gauges.LastUpdateGauge;
import se.tink.libraries.metrics.types.gauges.OptionalGaugeSampler;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class QsealcSignerHttpClient {
    private static final Logger log = LoggerFactory.getLogger(QsealcSignerHttpClient.class);
    private static IdleConnectionMonitorThread staleMonitor;
    static CloseableHttpClient httpClient;
    static QsealcSignerHttpClient qsealcSignerHttpClient = new QsealcSignerHttpClient();

    static synchronized QsealcSignerHttpClient create(InternalEidasProxyConfiguration conf) {
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
                connectionManager.setMaxTotal(200);
                connectionManager.setDefaultMaxPerRoute(200);

                try {
                    new MetricCollector().register(MetricId.newId("qsealc_signer_http_connection_pool_status_pending"), LastUpdateGauge.class, new OptionalGaugeSampler(()-> Optional.of(connectionManager.getTotalStats().getPending())));
                    new MetricCollector().register(MetricId.newId("qsealc_signer_http_connection_pool_status_available"), LastUpdateGauge.class, new OptionalGaugeSampler(()-> Optional.of(connectionManager.getTotalStats().getAvailable())));
                    new MetricCollector().register(MetricId.newId("qsealc_signer_http_connection_pool_status_leased"), LastUpdateGauge.class, new OptionalGaugeSampler(()-> Optional.of(connectionManager.getTotalStats().getLeased())));
                    new MetricCollector().register(MetricId.newId("qsealc_signer_http_connection_pool_status_max"), LastUpdateGauge.class, new OptionalGaugeSampler(()-> Optional.of(connectionManager.getTotalStats().getMax())));
                } catch (Exception e) {
                    log.info("Failed to register metric collector for qsealc signer httpclient");
                }

                RequestConfig config =
                        RequestConfig.custom()
                                .setConnectTimeout(10 * 1000)
                                .setConnectionRequestTimeout(10 * 1000)
                                .setSocketTimeout(10 * 1000)
                                .build();

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
                            return 5 * 60 * 1000;
                        };

                httpClient =
                        HttpClients.custom()
                                .setConnectionManager(connectionManager)
                                .setKeepAliveStrategy(ttl)
                                .setDefaultRequestConfig(config)
                                .build();

                if (staleMonitor == null) {
                    staleMonitor = new IdleConnectionMonitorThread(connectionManager);
                    staleMonitor.start();
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return qsealcSignerHttpClient;
    }

    public CloseableHttpResponse execute(HttpPost post) throws IOException {
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            return response;
        }
    }

    private static class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;
        private volatile AtomicBoolean shutdown = new AtomicBoolean(false);

        IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown.get()) {
                    synchronized (this) {
                        wait(5000);
                        connMgr.closeExpiredConnections();
                        connMgr.closeIdleConnections(60, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                shutdown();
            }
        }

        public void shutdown() {
            shutdown.set(true);
            synchronized (this) {
                notifyAll();
            }
        }
    }
}
