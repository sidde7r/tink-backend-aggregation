package se.tink.backend.aggregation.cluster.jersey;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.cluster.annotations.ClientContext;
import se.tink.backend.aggregation.cluster.exceptions.ClientNotValid;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.providers.ClientConfigurationProvider;

public class JerseyClientProvider extends AbstractHttpContextInjectable<ClientInfo>
        implements InjectableProvider<ClientContext, Type> {
    private static final String CLIENT_API_KEY_HEADER = "X-Tink-Client-Api-Key";
    private static final String APP_ID_HEADER_KEY = "X-Tink-App-Id";

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
        String appId = request.getHeaderValue(APP_ID_HEADER_KEY);

        logger.info("The apiKey: {} & appId: {} while fetching from header param.", apiKey, appId);

        String name = request.getHeaderValue(CLUSTER_NAME_HEADER);
        String environment = request.getHeaderValue(CLUSTER_ENVIRONMENT_HEADER);
        ClusterId clusterId = ClusterId.of(name, environment);

        if (!Strings.isNullOrEmpty(apiKey)) {
            ClientInfo clientInfoUsingApiKey = getClientInfoUsingApiKey(apiKey, appId);
            if (clusterId.isValidId()
                    && !clientInfoUsingApiKey.getClusterId().equalsIgnoreCase(clusterId.getId())) {
                logger.error(
                        "The apiKey: {} & appId: {} has inconsistent clusterid configured in database.",
                        apiKey,
                        appId);
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            return clientInfoUsingApiKey;
        }

        logger.error("Received a missing api key for {} {}.", name, environment);
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    private ClientInfo getClientInfoUsingApiKey(String apiKey, String appId) {
        try {
            ClientConfiguration clientConfig =
                    clientConfigurationProvider.getClientConfiguration(apiKey);
            return createClientInfo(clientConfig, appId);
        } catch (ClientNotValid e) {
            logger.error("Api key {} is not valid. no entry found in database.", apiKey);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    private ClientInfo createClientInfo(ClientConfiguration clientConfiguration, String appId) {
        String clientName = clientConfiguration.getClientName();
        String clusterId = clientConfiguration.getClusterId();
        String aggregatorId = clientConfiguration.getAggregatorId();

        return ClientInfo.of(clientName, clusterId, aggregatorId, appId);
    }
}
