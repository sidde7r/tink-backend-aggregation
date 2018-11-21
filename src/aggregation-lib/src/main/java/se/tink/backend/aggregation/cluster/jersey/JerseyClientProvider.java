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
import se.tink.backend.aggregation.cluster.exceptions.ClientNotValid;
import se.tink.backend.aggregation.cluster.exceptions.ClusterNotValid;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.providers.ClientConfigurationProvider;

public class JerseyClientProvider extends AbstractHttpContextInjectable<ClientInfo>
        implements InjectableProvider<ClientContext, Type> {
    private static final String CLIENT_API_KEY_HEADER = "X-Tink-Client-Api-Key";

    private static final String CLUSTER_NAME_HEADER = ClusterId.CLUSTER_NAME_HEADER;
    private static final String CLUSTER_ENVIRONMENT_HEADER = ClusterId.CLUSTER_ENVIRONMENT_HEADER;
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

        if (!Strings.isNullOrEmpty(apiKey)) {
            return getClientInfoUsingApiKey(apiKey);
        }

        String name = request.getHeaderValue(CLUSTER_NAME_HEADER);
        String environment = request.getHeaderValue(CLUSTER_ENVIRONMENT_HEADER);
        logger.error("Received a missing api key for {} {}.", name, environment);
        return getClientInfoUsingClusterInfo(name, environment);
    }

    private ClientInfo getClientInfoUsingApiKey(String apiKey) {
        try{
            ClientConfiguration clientConfig = clientConfigurationProvider.getClientConfiguration(apiKey);
            logger.info("Received request for client: {}", clientConfig.getClientName());
            return convertFromClientConfiguration(clientConfig);
        } catch (ClientNotValid e) {
            // FIXME: we log it at the moment to validate data is in place. later should be handled throwing exception
            logger.error("Api key {} is not valid. no entry found in database.", apiKey);
        }
        return null;
    }

    private ClientInfo getClientInfoUsingClusterInfo(String name, String env) {
        try{
            ClientConfiguration clientConfig = clientConfigurationProvider.getClientConfiguration(name, env);
            return convertFromClientConfiguration(clientConfig);
        } catch (ClusterNotValid e) {
            // FIXME: we log it at the moment to validate data is in place. later should be handled throwing exception
            logger.error("Cluster {}-{} is not supported in multi client. no entry found in database", name, env);
        }
        return null;
    }

    private ClientInfo convertFromClientConfiguration(ClientConfiguration clientConfiguration) {
        String clientName = clientConfiguration.getClientName();
        String clusterId = clientConfiguration.getClusterId();
        String aggregatorId = clientConfiguration.getAggregatorId();

        return ClientInfo.of(clientName, clusterId, aggregatorId);
    }
}
