package se.tink.backend.aggregation.agents.modules.providers;

import com.google.inject.Inject;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import java.io.PrintStream;
import java.net.URI;
import java.util.function.Function;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.utils.jersey.LoggingFilter;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.net.client.TinkApacheHttpClient4;

public class LegacyAgentProductionStrategy implements LegacyAgentStrategyInterface {

    private final CredentialsRequest request;
    private final CompositeAgentContext context;

    @Inject
    public LegacyAgentProductionStrategy(
            CredentialsRequest request, CompositeAgentContext context) {
        this.context = context;
        this.request = request;
    }

    @Override
    public Function<String, URI> getLegacyHostStrategy() {
        return URI::create;
    }

    @Override
    public Function<ApacheHttpClient4Config, TinkApacheHttpClient4> getLegacyHttpClientStrategy() {
        return clientConfig -> {
            clientConfig.getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, null);
            TinkApacheHttpClient4 client = TinkApacheHttpClient4.create(clientConfig);

            client.setChunkedEncodingSize(null);
            client.setReadTimeout(30000);
            client.setConnectTimeout(10000);

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
