package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.client.ClientAggregationServiceFactory;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.jersey.utils.JerseyUtils;
import se.tink.backend.guice.annotations.AggregationConfiguration;

public class AggregationServiceFactoryProvider implements Provider<AggregationServiceFactory> {
    private final EndpointConfiguration endpoint;
    private final CuratorFramework coordinationClient;

    @Inject
    public AggregationServiceFactoryProvider(@AggregationConfiguration EndpointConfiguration endpoint,
            CuratorFramework coordinationClient) {
        this.endpoint = endpoint;
        this.coordinationClient = coordinationClient;
    }

    @Override
    public AggregationServiceFactory get() {
        Client client = JerseyUtils.getClient(endpoint.getPinnedCertificates());
        JerseyUtils.registerAPIAccessToken(client, endpoint.getAccessToken());
        return new ClientAggregationServiceFactory(new DiscoveredWebServiceClassBuilder(
                coordinationClient, client, AggregationServiceFactory.SERVICE_NAME));
    }
}
