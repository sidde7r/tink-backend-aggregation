package se.tink.backend.aggregationcontroller.resources;

import com.google.inject.Inject;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregationcontroller.client.AggregationServiceClient;
import se.tink.backend.aggregationcontroller.configuration.AggregationClusterConfiguration;
import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerProviderService;
import se.tink.backend.core.ProviderConfiguration;

public class AggregationControllerProviderServiceResource implements AggregationControllerProviderService {
    private final AggregationServiceClient serviceClient;
    private final boolean useAggregationCluster;

    @Inject
    public AggregationControllerProviderServiceResource(AggregationServiceClient serviceClient,
            AggregationClusterConfiguration aggregationConfiguration) {
        this.serviceClient = serviceClient;
        this.useAggregationCluster = aggregationConfiguration.isEnabled();
    }

    @Override
    public List<ProviderConfiguration> list() {
        return serviceClient.list(useAggregationCluster);
    }

    @Override
    public List<ProviderConfiguration> listByMarket(String market) {
        return serviceClient.listByMarket(useAggregationCluster, market);
    }

    @Override
    public ProviderConfiguration getProviderByName(String providerName) {
        try {
            return serviceClient.getProviderByName(useAggregationCluster, providerName);
        } catch (UniformInterfaceException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
