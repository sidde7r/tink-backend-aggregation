package se.tink.backend.aggregation.agents.modules.providers;

import com.google.inject.Inject;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.function.Function;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;
import se.tink.backend.aggregation.agents.utils.jersey.LoggingFilter;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.net.client.TinkApacheHttpClient4;

public class LegacyAgentWiremockStrategy implements LegacyAgentStrategyInterface {

    private final FakeBankSocket fakeBankSocket;
    private final CredentialsRequest request;
    private final CompositeAgentContext context;

    @Inject
    public LegacyAgentWiremockStrategy(
            CredentialsRequest request,
            CompositeAgentContext context,
            FakeBankSocket fakeBankSocket) {
        this.context = context;
        this.request = request;
        this.fakeBankSocket = fakeBankSocket;
    }

    @Override
    public Function<String, URI> getLegacyHostStrategy() {
        return s -> {
            URI uri = URI.create(s);
            try {
                return new URI(
                        uri.getScheme().toLowerCase(Locale.US),
                        fakeBankSocket.get(),
                        uri.getPath(),
                        uri.getQuery(),
                        uri.getFragment());
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return uri;
            }
        };
    }

    @Override
    public Function<ApacheHttpClient4Config, TinkApacheHttpClient4> getLegacyHttpClientStrategy() {
        return clientConfig -> {
            SSLContext sslContext;
            try {
                sslContext =
                        SSLContexts.custom()
                                .loadTrustMaterial(null, (chain, authType) -> true)
                                .build();
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new IllegalStateException(e);
            }

            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(
                    new Scheme(
                            "https",
                            443,
                            new SSLSocketFactory(
                                    sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));

            PoolingClientConnectionManager connectionManager =
                    new PoolingClientConnectionManager(schemeRegistry);
            clientConfig
                    .getProperties()
                    .put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager);
            TinkApacheHttpClient4 client = TinkApacheHttpClient4.create(clientConfig);

            client.setChunkedEncodingSize(null);
            try {
                if (context.getLogOutputStream() != null) {
                    client.addFilter(
                            new LoggingFilter(
                                    new PrintStream(context.getLogOutputStream(), true, "UTF-8"),
                                    context.getLogMasker(),
                                    LogMaskerImpl.shouldLog(request.getProvider())));
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(LegacyAgentProductionStrategy.class)
                        .error("Could not add logging filter", e);
            }

            return client;
        };
    }
}
