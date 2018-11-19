package se.tink.backend.aggregation.cluster.jersey;

import com.google.inject.Inject;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.cluster.annotations.ClientContext;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.providers.ClientConfigurationProvider;
public class JerseyClientProvider extends AbstractHttpContextInjectable<ClientInfo>
        implements InjectableProvider<ClientContext, Type> {

    private static final String CLIENT_API_KEY_HEADER = "X-Tink-Client-Api-Key";

    protected static final String CLUSTER_NAME_HEADER = ClusterId.CLUSTER_NAME_HEADER;
    protected static final String CLUSTER_ENVIRONMENT_HEADER = ClusterId.CLUSTER_ENVIRONMENT_HEADER;

    private final Logger logger = LoggerFactory.getLogger(JerseyClientProvider.class);
    private final ClientConfigurationProvider clientConfigurationProvider;

    @Inject
    JerseyClientProvider(ClientConfigurationProvider clientConfigurationProvider) {
        this.clientConfigurationProvider = clientConfigurationProvider;
    }

    @Override
    public Injectable<ClientInfo> getInjectable(ComponentContext ic, ClientContext a, Type c) {
        return c.equals(ClientInfo.class) ? this : null;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public ClientInfo getValue(HttpContext c) {
        HttpRequestContext request = c.getRequest();
        String apiKey = request.getHeaderValue(CLIENT_API_KEY_HEADER);

        // check if apikey is in header
        if (Strings.isNullOrEmpty(apiKey)) {
            String name = request.getHeaderValue(CLUSTER_NAME_HEADER);
            String environment = request.getHeaderValue(CLUSTER_ENVIRONMENT_HEADER);
            return getClientInfoUsingClusterInfo(name, environment);
        }

        // check if apikey is in storage
        if (!clientConfigurationProvider.isValidClientKey(apiKey)) {
            logger.error("Can not find api key {} in database.", apiKey);
            return null;
        }

        ClientInfo clientInfo = convertFromClientConfiguration(clientConfigurationProvider.getClientConfiguration(apiKey));
        logger.info("Client info retrived for {}", clientInfo.getClientName());
        return clientInfo;
    }

    private ClientInfo getClientInfoUsingClusterInfo(String name, String environment) {

        String clusterId = String.format("%s-%s", name, environment);

        logger.warn("Received a missing api key for {} {}. generating using clusterid {}. ", name, environment,
                clusterId);
        if (!clientConfigurationProvider.isValidName(clusterId)) {
            logger.error("Can not find cluster id {} in database.", clusterId);
            return null;
        }

        return convertFromClientConfiguration(clientConfigurationProvider.getClientConfiguration(clusterId));
    }

    private ClientInfo convertFromClientConfiguration(ClientConfiguration clientConfiguration) {
        String clientName = clientConfiguration.getClientName();
        String clusterId = clientConfiguration.getClusterId();
        String aggregatorId = clientConfiguration.getAggregatorId();

        return ClientInfo.of(clientName, clusterId, aggregatorId);
    }
}
